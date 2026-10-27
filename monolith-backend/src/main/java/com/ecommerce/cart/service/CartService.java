package com.ecommerce.cart.service;

import com.ecommerce.cart.client.ProductClient;
import com.ecommerce.cart.dto.CartDtos.*;
import com.ecommerce.cart.entity.*;
import com.ecommerce.cart.exception.*;
import com.ecommerce.cart.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.*;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository carts;
    private final CartItemRepository items;
    private final ProductClient products;

    private Cart getOrCreateCart(Long userId) {
        return carts.findByUserId(userId).orElseGet(() ->
                carts.save(Cart.builder().userId(userId).items(new ArrayList<>()).build()));
    }

    @Transactional
    public CartResponse addItem(Long userId, AddItemRequest req) {
        ProductView p = products.getProduct(req.getProductId());
        if (p.getActive() != null && !p.getActive()) throw new BadRequestException("Product is inactive");
        if (p.getStock() == null || p.getStock() < req.getQuantity())
            throw new BadRequestException("Insufficient stock. Available: " + p.getStock());

        Cart cart = getOrCreateCart(userId);
        Optional<CartItem> existing = cart.getItems().stream()
                .filter(i -> i.getProductId().equals(req.getProductId())).findFirst();
        if (existing.isPresent()) {
            CartItem item = existing.get();
            int newQty = item.getQuantity() + req.getQuantity();
            if (p.getStock() < newQty) throw new BadRequestException("Insufficient stock. Available: " + p.getStock());
            item.setQuantity(newQty);
            item.setPrice(p.getPrice());
            items.save(item);
        } else {
            CartItem item = CartItem.builder()
                    .cart(cart).productId(req.getProductId())
                    .quantity(req.getQuantity()).price(p.getPrice()).build();
            cart.getItems().add(item);
            items.save(item);
        }
        return toResponse(carts.save(cart));
    }

    @Transactional(readOnly = true)
    public CartResponse getCart(Long userId) {
        Cart cart = carts.findByUserId(userId).orElseGet(() ->
                Cart.builder().id(0L).userId(userId).items(new ArrayList<>()).build());
        return toResponseWithNames(cart);
    }

    @Transactional
    public CartResponse updateItem(Long userId, Long itemId, UpdateItemRequest req) {
        Cart cart = carts.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));
        CartItem item = items.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found"));
        if (!cart.getItems().stream().anyMatch(i -> i.getId().equals(itemId)))
            throw new BadRequestException("Item does not belong to user's cart");
        ProductView p = products.getProduct(item.getProductId());
        if (p.getStock() < req.getQuantity())
            throw new BadRequestException("Insufficient stock. Available: " + p.getStock());
        item.setQuantity(req.getQuantity());
        item.setPrice(p.getPrice());
        items.save(item);
        return toResponse(cart);
    }

    @Transactional
    public void removeItem(Long userId, Long itemId) {
        Cart cart = carts.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));
        boolean owns = cart.getItems().stream().anyMatch(i -> i.getId().equals(itemId));
        if (!owns) throw new BadRequestException("Item does not belong to user's cart");
        // Single-row delete only — never touch the rest of the cart.
        items.deleteById(itemId);
    }

    @Transactional
    public void clearCart(Long userId) {
        carts.findByUserId(userId).ifPresent(carts::delete);
    }

    private CartResponse toResponse(Cart cart) {
        List<CartItemResponse> list = cart.getItems().stream().map(i ->
                CartItemResponse.builder().id(i.getId()).productId(i.getProductId())
                        .quantity(i.getQuantity()).price(i.getPrice())
                        .lineTotal(i.getPrice().multiply(BigDecimal.valueOf(i.getQuantity())))
                        .build()).toList();
        BigDecimal total = list.stream().map(CartItemResponse::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return CartResponse.builder().cartId(cart.getId()).userId(cart.getUserId())
                .items(list).total(total).build();
    }

    private CartResponse toResponseWithNames(Cart cart) {
        // enrich names best-effort (skip failures so GET cart never 500s)
        List<CartItemResponse> list = new ArrayList<>();
        for (var i : cart.getItems()) {
            String name = null;
            try { name = products.getProduct(i.getProductId()).getName(); } catch (Exception ignored) {}
            list.add(CartItemResponse.builder().id(i.getId()).productId(i.getProductId())
                    .productName(name).quantity(i.getQuantity()).price(i.getPrice())
                    .lineTotal(i.getPrice().multiply(BigDecimal.valueOf(i.getQuantity()))).build());
        }
        BigDecimal total = list.stream().map(CartItemResponse::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return CartResponse.builder().cartId(cart.getId()).userId(cart.getUserId())
                .items(list).total(total).build();
    }
}
