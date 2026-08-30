package com.ecommerce.project.dto;

import jakarta.validation.constraints.*;

public record RegisterRequest(@Email @NotBlank String email, @NotBlank @Size(min = 6, max = 128) String password,
                              @NotBlank @Size(max = 120) String displayName) { }
