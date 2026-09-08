package com.ecommerce.order.client;

import com.ecommerce.order.dto.OrderDtos.ProductView;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "PRODUCT-SERVICE", path = "/api/products")
public interface ProductClient {
    @GetMapping("/{id}")
    ProductView getProduct(@PathVariable("id") Long id);

    // Stock reservation (added in product-service Phase 13)
    @PostMapping("/{id}/decrease-stock")
    void decreaseStock(@PathVariable("id") Long id, @RequestParam("quantity") int quantity);

    @PostMapping("/{id}/increase-stock")
    void increaseStock(@PathVariable("id") Long id, @RequestParam("quantity") int quantity);
}
