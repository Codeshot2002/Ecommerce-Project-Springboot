package com.ecommerce.project.dto;

import java.util.List;
public record UserResponse(Long id, String email, String displayName, List<String> roles) { }
