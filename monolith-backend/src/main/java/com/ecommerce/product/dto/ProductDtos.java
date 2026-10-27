package com.ecommerce.product.dto;

import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;
import java.util.Map;

public class ProductDtos {

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class CategoryRequest {
        @NotBlank private String name;
        private String description;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class CategoryResponse {
        private Long id;
        private String name;
        private String description;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class ProductRequest {
        @NotBlank private String name;
        private String description;
        @NotNull @DecimalMin("0.01") private BigDecimal price;
        @NotNull @Min(0) private Integer stock;
        private Long categoryId;
        private String imageUrl;
        private Boolean active;
        private Map<String, String> specifications;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class ProductResponse {
        private Long id;
        private String name;
        private String description;
        private BigDecimal price;
        private Integer stock;
        private Long categoryId;
        private String categoryName;
        private String imageUrl;
        private Boolean active;
        private Map<String, String> specifications;
    }
}
