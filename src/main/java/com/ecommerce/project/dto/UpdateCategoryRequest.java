package com.ecommerce.project.dto;

import jakarta.validation.constraints.Size;

/** Fields accepted by the partial category-update endpoint. */
public record UpdateCategoryRequest(
        @Size(min = 1, max = 100, message = "name must contain between 1 and 100 characters")
        String name,
        @Size(max = 1_000, message = "description must contain at most 1000 characters")
        String description
) {
}
