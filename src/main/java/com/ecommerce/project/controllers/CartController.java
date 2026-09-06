package com.ecommerce.project.controllers;

import com.ecommerce.project.dto.CartRequest;
import com.ecommerce.project.dto.CartResponse;
import com.ecommerce.project.service.CartService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
public class CartController {
    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @PostMapping("/api/cart")
    public ResponseEntity<String> createCart(Authentication authentication, @Valid @RequestBody CartRequest cartRequest) {
        cartService.addToCart(authentication.getName(), cartRequest);
        return ResponseEntity.ok("Product added to cart");
    }

    @GetMapping("/api/cart")
    public ResponseEntity<CartResponse> getCart(Authentication authentication) {
        return ResponseEntity.ok(cartService.getCart(authentication.getName()));
    }

    @DeleteMapping("/api/cart")
    public ResponseEntity<String> emptyCartItem(Authentication authentication, @RequestParam Long productId) {
        String removedItem = cartService.removeCartItem(authentication.getName(), productId);
        return ResponseEntity.ok(String.format("Cart Item [%s] is removed", removedItem));
    }
}
