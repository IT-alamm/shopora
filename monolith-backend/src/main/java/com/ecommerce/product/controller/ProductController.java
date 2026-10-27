package com.ecommerce.product.controller;

import com.ecommerce.product.dto.ProductDtos.*;
import com.ecommerce.product.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class ProductController {

    private final ProductService service;

    // Public: GET /api/products?keyword=&categoryId=&minPrice=&maxPrice=&page=0&size=10&sort=price,asc
    @GetMapping("/api/products")
    public Page<ProductResponse> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id,asc") String sort) {
        return service.search(keyword, categoryId, minPrice, maxPrice, page, size, sort);
    }

    @GetMapping("/api/products/{id}")
    public ProductResponse getOne(@PathVariable Long id) {
        return service.getProduct(id);
    }

    @GetMapping("/api/products/health")
    public String health() { return "product-service OK"; }

    // Admin (TODO: enforce ROLE_ADMIN via gateway header X-User-Role once UI ready; currently open for dev)
    @PostMapping("/api/products")
    @ResponseStatus(HttpStatus.CREATED)
    public ProductResponse create(@Valid @RequestBody ProductRequest req,
                                  @RequestHeader(value = "X-User-Role", required = false) String role) {
        return service.createProduct(req);
    }

    @PutMapping("/api/products/{id}")
    public ProductResponse update(@PathVariable Long id, @RequestBody ProductRequest req,
                                  @RequestHeader(value = "X-User-Role", required = false) String role) {
        return service.updateProduct(id, req);
    }

    @DeleteMapping("/api/products/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id,
                       @RequestHeader(value = "X-User-Role", required = false) String role) {
        service.deleteProduct(id);
    }

    // Stock reservation for order-service (Feign). Internal — not via frontend directly.
    @PostMapping("/api/products/{id}/decrease-stock")
    public ProductResponse decreaseStock(@PathVariable Long id, @RequestParam int quantity) {
        return service.decreaseStock(id, quantity);
    }

    @PostMapping("/api/products/{id}/increase-stock")
    public ProductResponse increaseStock(@PathVariable Long id, @RequestParam int quantity) {
        return service.increaseStock(id, quantity);
    }

    // Categories
    @GetMapping("/api/categories")
    public List<CategoryResponse> listCategories() {
        return service.listCategories();
    }

    @PostMapping("/api/categories")
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryResponse createCategory(@Valid @RequestBody CategoryRequest req) {
        return service.createCategory(req);
    }
}
