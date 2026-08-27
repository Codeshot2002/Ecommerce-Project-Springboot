package com.ecommerce.project.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

/** The payload accepted by the checkout endpoint. */
public record PlaceOrderRequest(
        @NotEmpty(message = "An order must contain at least one item")
        List<@Valid PlaceOrderItemRequest> items
) {
}
