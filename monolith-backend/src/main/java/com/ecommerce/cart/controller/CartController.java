package com.ecommerce.cart.controller;

import com.ecommerce.cart.dto.CartDtos.*;
import com.ecommerce.cart.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService service;

    // Gateway injects X-User-Id from JWT; direct testing can pass header manually.
    // Dev fallback userId=1 if header missing.
    private Long resolveUserId(String header) {
        if (header == null || header.isBlank() || "null".equalsIgnoreCase(header)) return 1L;
        try { return Long.parseLong(header); } catch (NumberFormatException e) { return 1L; }
    }

    @PostMapping("/items")
    @ResponseStatus(HttpStatus.CREATED)
    public CartResponse add(@RequestHeader(value = "X-User-Id", required = false) String userId,
                            @Valid @RequestBody AddItemRequest req) {
        return service.addItem(resolveUserId(userId), req);
    }

    @GetMapping
    public CartResponse get(@RequestHeader(value = "X-User-Id", required = false) String userId) {
        return service.getCart(resolveUserId(userId));
    }

    @GetMapping("/health")
    public String health() { return "cart-service OK"; }

    @PutMapping("/items/{itemId}")
    public CartResponse update(@RequestHeader(value = "X-User-Id", required = false) String userId,
                               @PathVariable Long itemId,
                               @Valid @RequestBody UpdateItemRequest req) {
        return service.updateItem(resolveUserId(userId), itemId, req);
    }

    @DeleteMapping("/items/{itemId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void remove(@RequestHeader(value = "X-User-Id", required = false) String userId,
                       @PathVariable Long itemId) {
        service.removeItem(resolveUserId(userId), itemId);
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void clear(@RequestHeader(value = "X-User-Id", required = false) String userId) {
        service.clearCart(resolveUserId(userId));
    }
}
