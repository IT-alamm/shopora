package com.ecommerce.cart.client;

import com.ecommerce.cart.dto.CartDtos.ProductView;
import com.ecommerce.product.dto.ProductDtos.ProductResponse;
import com.ecommerce.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProductClient {
    private final ProductService productService;

    public ProductView getProduct(Long id) {
        ProductResponse p = productService.getProduct(id);
        return ProductView.builder().id(p.getId()).name(p.getName()).price(p.getPrice())
                .stock(p.getStock()).active(p.getActive()).build();
    }
}