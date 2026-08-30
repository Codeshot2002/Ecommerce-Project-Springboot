package com.ecommerce.project.dto;

/** Internal service result; refreshToken is written only to an HttpOnly cookie. */
public record LoginResult(AuthResponse response, String refreshToken) { }
