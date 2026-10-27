package com.ecommerce.order.client;

import com.ecommerce.order.dto.OrderDtos.ProductView;
import com.ecommerce.product.dto.ProductDtos.ProductResponse;
import com.ecommerce.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderProductClient {
    private final ProductService productService;

    public ProductView getProduct(Long id) {
        ProductResponse p = productService.getProduct(id);
        return ProductView.builder().id(p.getId()).name(p.getName()).price(p.getPrice())
                .stock(p.getStock()).active(p.getActive()).build();
    }

    public void decreaseStock(Long id, int quantity) { productService.decreaseStock(id, quantity); }
    public void increaseStock(Long id, int quantity) { productService.increaseStock(id, quantity); }
}