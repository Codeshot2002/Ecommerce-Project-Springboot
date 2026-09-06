package com.ecommerce.project.dto;

import java.util.List;

public record CartResponse(
        List<CartItem> cartItems, Long totalPrice) {
}
