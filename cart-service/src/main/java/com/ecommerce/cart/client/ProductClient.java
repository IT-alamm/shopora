package com.ecommerce.cart.client;

import com.ecommerce.cart.dto.CartDtos.ProductView;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "PRODUCT-SERVICE", path = "/api/products")
public interface ProductClient {
    @GetMapping("/{id}")
    ProductView getProduct(@PathVariable("id") Long id);
}
