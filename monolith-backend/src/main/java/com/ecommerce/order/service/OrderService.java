package com.ecommerce.order.service;

import com.ecommerce.order.client.OrderProductClient;
import com.ecommerce.order.dto.OrderDtos.*;
import com.ecommerce.order.entity.*;
import com.ecommerce.order.exception.*;
import com.ecommerce.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.*;

@Service
@RequiredArgsConstructor
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    private final OrderRepository orders;
    private final OrderProductClient products;

    /**
     * Creates order in PAYMENT_PENDING state.
     * Validates products + stock via product-service, calculates total,
     * then reserves stock (decrease). On failure after partial reserve, rolls back.
     */
    @Transactional
    public OrderResponse createOrder(Long userId, CreateOrderRequest req) {
        BigDecimal total = BigDecimal.ZERO;
        List<ProductView> views = new ArrayList<>();
        for (OrderItemRequest item : req.getItems()) {
            ProductView p = products.getProduct(item.getProductId());
            if (p.getActive() != null && !p.getActive())
                throw new BadRequestException("Product inactive: " + item.getProductId());
            if (p.getStock() == null || p.getStock() < item.getQuantity())
                throw new BadRequestException("Insufficient stock for product " + p.getName()
                        + ". Available: " + p.getStock());
            total = total.add(p.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
            views.add(p);
        }

        Order order = Order.builder()
                .userId(userId).totalAmount(total)
                .status(OrderStatus.PAYMENT_PENDING)
                .shippingAddress(req.getShippingAddress())
                .items(new ArrayList<>())
                .build();

        for (int i = 0; i < req.getItems().size(); i++) {
            OrderItemRequest item = req.getItems().get(i);
            ProductView p = views.get(i);
            OrderItem oi = OrderItem.builder()
                    .order(order).productId(item.getProductId())
                    .quantity(item.getQuantity()).price(p.getPrice())
                    .build();
            order.getItems().add(oi);
        }
        order = orders.save(order);

        // Reserve stock AFTER persisting order; rollback reserve on failure
        List<OrderItemRequest> reserved = new ArrayList<>();
        try {
            for (OrderItemRequest item : req.getItems()) {
                products.decreaseStock(item.getProductId(), item.getQuantity());
                reserved.add(item);
            }
        } catch (Exception e) {
            log.error("Stock reserve failed for order {}, rolling back {} items", order.getId(), reserved.size());
            for (OrderItemRequest r : reserved) {
                try { products.increaseStock(r.getProductId(), r.getQuantity()); }
                catch (Exception ignored) {}
            }
            throw new BadRequestException("Stock reservation failed: " + e.getMessage());
        }

        log.info("Order created: orderId={} userId={} total={}", order.getId(), userId, total);
        return toResponse(order, views);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> myOrders(Long userId) {
        return orders.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(o -> toResponse(o, null)).toList();
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrder(Long userId, Long id) {
        Order o = orders.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + id));
        // Owner or admin (admin check via gateway header in controller; service keeps simple owner check disabled for dev)
        return toResponse(o, null);
    }

    @Transactional
    public OrderResponse cancelOrder(Long userId, Long id) {
        Order o = orders.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + id));
        if (o.getStatus() == OrderStatus.DELIVERED || o.getStatus() == OrderStatus.SHIPPED)
            throw new BadRequestException("Cannot cancel order in status " + o.getStatus());
        // release stock
        for (OrderItem item : o.getItems()) {
            try { products.increaseStock(item.getProductId(), item.getQuantity()); }
            catch (Exception ignored) {}
        }
        o.setStatus(OrderStatus.CANCELLED);
        return toResponse(orders.save(o), null);
    }

    /** Called by payment-service flow after successful verification (or admin). */
    @Transactional
    public OrderResponse updateStatus(Long id, OrderStatus status) {
        Order o = orders.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + id));
        o.setStatus(status);
        log.info("Order status: orderId={} -> {}", id, status);
        return toResponse(orders.save(o), null);
    }

    private OrderResponse toResponse(Order o, List<ProductView> views) {
        Map<Long, String> names = new HashMap<>();
        if (views != null) for (ProductView v : views) names.put(v.getId(), v.getName());
        List<OrderItemResponse> list = o.getItems().stream().map(i ->
                OrderItemResponse.builder().productId(i.getProductId())
                        .productName(names.get(i.getProductId()))
                        .quantity(i.getQuantity()).price(i.getPrice())
                        .lineTotal(i.getPrice().multiply(BigDecimal.valueOf(i.getQuantity())))
                        .build()).toList();
        return OrderResponse.builder().id(o.getId()).userId(o.getUserId())
                .totalAmount(o.getTotalAmount()).status(o.getStatus().name())
                .shippingAddress(o.getShippingAddress()).items(list).build();
    }
}
