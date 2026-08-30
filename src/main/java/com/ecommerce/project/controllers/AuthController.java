package com.ecommerce.project.controllers;

import com.ecommerce.project.config.AuthProperties;
import com.ecommerce.project.dto.*;
import com.ecommerce.project.models.User;
import com.ecommerce.project.service.AuthService;
import jakarta.servlet.http.*;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService auth;
    private final AuthProperties properties;

    public AuthController(AuthService auth, AuthProperties properties) {
        this.auth = auth;
        this.properties = properties;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request, HttpServletRequest http, HttpServletResponse response) {
        LoginResult result = auth.register(request, device(http));
        setRefresh(response, result.refreshToken());
        return ResponseEntity.ok(result.response());
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request, HttpServletRequest http, HttpServletResponse response) {
        LoginResult result = auth.login(request, device(http));
        setRefresh(response, result.refreshToken());
        return ResponseEntity.ok(result.response());
    }

    @PostMapping("/refresh")
    public AuthResponse refresh(@CookieValue(value = "refresh_token", required = false) String refresh, HttpServletRequest request, HttpServletResponse response) {
        if (refresh == null)
            throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.UNAUTHORIZED, "Refresh token is required");
        LoginResult result = auth.refresh(refresh, device(request));
        setRefresh(response, result.refreshToken());
        return result.response();
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@CookieValue(value = "refresh_token", required = false) String refresh, HttpServletResponse response) {
        auth.logout(refresh);
        Cookie c = new Cookie("refresh_token", "");
        c.setPath("/api/auth");
        c.setMaxAge(0);
        c.setHttpOnly(true);
        response.addCookie(c);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    public UserResponse me(Authentication authentication) {
        User user = auth.findUser(authentication.getName());
        return new UserResponse(user.getId(), user.getEmail(), user.getDisplayName(), java.util.List.of("ROLE_" + user.getRole()));
    }

    @GetMapping("/csrf")
    public CsrfToken csrf(CsrfToken token) {
        return token;
    }

    private String device(HttpServletRequest request) {
        String value = request.getHeader("User-Agent");
        return value == null ? null : value.substring(0, Math.min(value.length(), 255));
    }

    private void setRefresh(HttpServletResponse response, String raw) {
        if (raw == null) return;
        Cookie c = new Cookie("refresh_token", raw);
        c.setHttpOnly(true);
        c.setSecure(properties.cookieSecure());
        c.setPath("/api/auth");
        c.setMaxAge((int) (properties.refreshTokenDays() * 86400));
        c.setAttribute("SameSite", "Lax");
        response.addCookie(c);
    }
}
