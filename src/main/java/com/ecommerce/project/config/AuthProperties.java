package com.ecommerce.project.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.auth")
public record AuthProperties(long refreshTokenDays, boolean cookieSecure) { }
