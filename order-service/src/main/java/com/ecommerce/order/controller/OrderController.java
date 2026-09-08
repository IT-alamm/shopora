package com.ecommerce.order.controller;

import com.ecommerce.order.dto.OrderDtos.*;
import com.ecommerce.order.entity.OrderStatus;
import com.ecommerce.order.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService service;

    private Long resolveUserId(String header) {
        if (header == null || header.isBlank() || "null".equalsIgnoreCase(header)) return 1L;
        try { return Long.parseLong(header); } catch (NumberFormatException e) { return 1L; }
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponse create(@RequestHeader(value = "X-User-Id", required = false) String userId,
                                @Valid @RequestBody CreateOrderRequest req) {
        return service.createOrder(resolveUserId(userId), req);
    }

    @GetMapping
    public List<OrderResponse> myOrders(@RequestHeader(value = "X-User-Id", required = false) String userId) {
        return service.myOrders(resolveUserId(userId));
    }

    @GetMapping("/{id}")
    public OrderResponse getOne(@RequestHeader(value = "X-User-Id", required = false) String userId,
                                @PathVariable Long id) {
        return service.getOrder(resolveUserId(userId), id);
    }

    @PutMapping("/{id}/cancel")
    public OrderResponse cancel(@RequestHeader(value = "X-User-Id", required = false) String userId,
                                @PathVariable Long id) {
        return service.cancelOrder(resolveUserId(userId), id);
    }

    /** Internal/admin: payment flow calls this after verify SUCCESS -> CONFIRMED. */
    @PutMapping("/{id}/status")
    public OrderResponse updateStatus(@PathVariable Long id,
                                      @RequestParam OrderStatus status) {
        return service.updateStatus(id, status);
    }

    @GetMapping("/health")
    public String health() { return "order-service OK"; }
}
