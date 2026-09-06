package com.ecommerce.project.dto;

public record CartItem(Long productId, String productName, Integer quantity, Long price, String error) {
}
