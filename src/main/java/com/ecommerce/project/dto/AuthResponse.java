package com.ecommerce.project.dto;

import java.util.List;

public record AuthResponse(String accessToken, String tokenType, long expiresIn, UserResponse user) { }
