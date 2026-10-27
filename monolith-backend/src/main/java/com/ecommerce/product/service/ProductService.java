package com.ecommerce.product.service;

import com.ecommerce.product.dto.ProductDtos.*;
import com.ecommerce.product.entity.*;
import com.ecommerce.product.exception.ResourceNotFoundException;
import com.ecommerce.product.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository products;
    private final CategoryRepository categories;
    private final ObjectMapper objectMapper = new ObjectMapper();

    // ---- Category ----
    public CategoryResponse createCategory(CategoryRequest req) {
        if (categories.existsByName(req.getName())) throw new IllegalArgumentException("Category exists");
        Category c = categories.save(Category.builder().name(req.getName()).description(req.getDescription()).build());
        return toCategoryRes(c);
    }

    public List<CategoryResponse> listCategories() {
        return categories.findAll().stream().map(this::toCategoryRes).toList();
    }

    // ---- Product ----
    @Transactional
    public ProductResponse createProduct(ProductRequest req) {
        Category cat = req.getCategoryId() == null ? null :
                categories.findById(req.getCategoryId())
                        .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
        Product p = Product.builder()
                .name(req.getName()).description(req.getDescription())
                .price(req.getPrice()).stock(req.getStock())
                .category(cat).imageUrl(req.getImageUrl())
                .active(req.getActive() == null ? true : req.getActive())
                .specifications(toJson(req.getSpecifications()))
                .build();
        return toProductRes(products.save(p));
    }

    @Transactional
    public ProductResponse updateProduct(Long id, ProductRequest req) {
        Product p = products.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + id));
        if (req.getName() != null) p.setName(req.getName());
        if (req.getDescription() != null) p.setDescription(req.getDescription());
        if (req.getPrice() != null) p.setPrice(req.getPrice());
        if (req.getStock() != null) p.setStock(req.getStock());
        if (req.getCategoryId() != null) {
            Category cat = categories.findById(req.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
            p.setCategory(cat);
        }
        if (req.getImageUrl() != null) p.setImageUrl(req.getImageUrl());
        if (req.getActive() != null) p.setActive(req.getActive());
        if (req.getSpecifications() != null) p.setSpecifications(toJson(req.getSpecifications()));
        return toProductRes(products.save(p));
    }

    public void deleteProduct(Long id) {
        if (!products.existsById(id)) throw new ResourceNotFoundException("Product not found: " + id);
        products.deleteById(id);
    }

    @Transactional(readOnly = true)
    public ProductResponse getProduct(Long id) {
        Product p = products.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + id));
        // touch lazy category for mapping
        if (p.getCategory() != null) p.getCategory().getName();
        return toProductRes(p);
    }

    @Transactional(readOnly = true)
    public Page<ProductResponse> search(String keyword, Long categoryId,
                                        BigDecimal minPrice, BigDecimal maxPrice,
                                        int page, int size, String sort) {
        String[] parts = (sort == null ? "id,asc" : sort).split(",");
        Sort s = Sort.by(parts.length > 1 && parts[1].equalsIgnoreCase("desc") ?
                Sort.Direction.DESC : Sort.Direction.ASC, parts[0]);
        Pageable pageable = PageRequest.of(page, size, s);
        Specification<Product> spec = (root, query, cb) -> {
            var predicates = new java.util.ArrayList<jakarta.persistence.criteria.Predicate>();
            predicates.add(cb.isTrue(root.get("active")));
            String term = emptyToNull(keyword);
            if (term != null) predicates.add(cb.like(cb.lower(root.get("name")), "%" + term.toLowerCase(java.util.Locale.ROOT) + "%"));
            if (categoryId != null) predicates.add(cb.equal(root.get("category").get("id"), categoryId));
            if (minPrice != null) predicates.add(cb.greaterThanOrEqualTo(root.get("price"), minPrice));
            if (maxPrice != null) predicates.add(cb.lessThanOrEqualTo(root.get("price"), maxPrice));
            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };
        return products.findAll(spec, pageable).map(this::toProductRes);
    }

    private String emptyToNull(String v) { return (v == null || v.isBlank()) ? null : v; }

    /** Stock reservation with pessimistic lock to prevent oversell. Called by order-service. */
    @Transactional
    public ProductResponse decreaseStock(Long id, int quantity) {
        if (quantity <= 0) throw new IllegalArgumentException("quantity must be > 0");
        Product p = products.findByIdForUpdate(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + id));
        if (p.getStock() < quantity)
            throw new IllegalArgumentException("Insufficient stock. Available: " + p.getStock());
        p.setStock(p.getStock() - quantity);
        return toProductRes(products.save(p));
    }

    @Transactional
    public ProductResponse increaseStock(Long id, int quantity) {
        if (quantity <= 0) throw new IllegalArgumentException("quantity must be > 0");
        Product p = products.findByIdForUpdate(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + id));
        p.setStock(p.getStock() + quantity);
        return toProductRes(products.save(p));
    }

    private CategoryResponse toCategoryRes(Category c) {
        return CategoryResponse.builder().id(c.getId()).name(c.getName()).description(c.getDescription()).build();
    }

    private ProductResponse toProductRes(Product p) {
        return ProductResponse.builder()
                .id(p.getId()).name(p.getName()).description(p.getDescription())
                .price(p.getPrice()).stock(p.getStock())
                .categoryId(p.getCategory() == null ? null : p.getCategory().getId())
                .categoryName(p.getCategory() == null ? null : p.getCategory().getName())
                .imageUrl(p.getImageUrl()).active(p.getActive())
                .specifications(fromJson(p.getSpecifications()))
                .build();
    }

    private String toJson(Map<String, String> specs) {
        if (specs == null || specs.isEmpty()) return null;
        try { return objectMapper.writeValueAsString(specs); }
        catch (Exception e) { throw new IllegalArgumentException("Invalid specifications"); }
    }

    @SuppressWarnings("unchecked")
    private Map<String, String> fromJson(String json) {
        if (json == null || json.isBlank()) return null;
        try { return objectMapper.readValue(json, Map.class); }
        catch (Exception e) { return null; }
    }
}
