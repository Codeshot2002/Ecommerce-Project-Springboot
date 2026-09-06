package com.ecommerce.project.service;

import com.ecommerce.project.dto.CartRequest;
import com.ecommerce.project.dto.CartResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

public interface CartService {
    public void addToCart(String email, CartRequest cartRequest);

    public CartResponse getCart(String email);

    public String removeCartItem(String name, Long productId);
}
