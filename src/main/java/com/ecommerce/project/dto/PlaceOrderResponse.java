package com.ecommerce.project.dto;

/** Deliberately exposes an API result instead of returning the JPA entity graph. */
public record PlaceOrderResponse(Long orderId, String message) {
}
