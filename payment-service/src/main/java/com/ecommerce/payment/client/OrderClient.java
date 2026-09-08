package com.ecommerce.payment.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "ORDER-SERVICE", path = "/api/orders")
public interface OrderClient {
    @PutMapping("/{id}/status")
    void updateStatus(@PathVariable("id") Long id, @RequestParam("status") String status);
}
