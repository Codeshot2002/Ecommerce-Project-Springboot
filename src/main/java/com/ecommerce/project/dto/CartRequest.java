package com.ecommerce.project.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CartRequest(@NotNull Long productId, @Positive Integer quantity) {
}
