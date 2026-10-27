package com.ecommerce.payment.client;

import com.ecommerce.order.entity.OrderStatus;
import com.ecommerce.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderClient {
    private final OrderService orderService;

    public void updateStatus(Long id, String status) {
        orderService.updateStatus(id, OrderStatus.valueOf(status));
    }
}