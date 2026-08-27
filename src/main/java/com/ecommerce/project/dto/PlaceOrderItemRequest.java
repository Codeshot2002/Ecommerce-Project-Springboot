package com.ecommerce.project.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/** One requested product and its quantity; this is API input, not a JPA entity. */
public record PlaceOrderItemRequest(
        @NotNull(message = "Product id is required") Long productId,
        @NotNull(message = "Quantity is required")
        @Positive(message = "Quantity must be greater than zero") Long quantity
) {
}
