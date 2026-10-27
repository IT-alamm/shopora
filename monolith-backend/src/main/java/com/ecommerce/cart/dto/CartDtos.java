package com.ecommerce.cart.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;
import java.util.List;

public class CartDtos {

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class AddItemRequest {
        @NotNull private Long productId;
        @NotNull @Min(1) private Integer quantity;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class UpdateItemRequest {
        @NotNull @Min(1) private Integer quantity;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class CartItemResponse {
        private Long id;
        private Long productId;
        private String productName;
        private Integer quantity;
        private BigDecimal price;
        private BigDecimal lineTotal;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class CartResponse {
        private Long cartId;
        private Long userId;
        private List<CartItemResponse> items;
        private BigDecimal total;
    }

    // Minimal view of product-service GET /api/products/{id} response
    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class ProductView {
        private Long id;
        private String name;
        private BigDecimal price;
        private Integer stock;
        private Boolean active;
    }
}
