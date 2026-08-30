package com.ecommerce.project.service;

import com.ecommerce.project.config.AuthProperties;
import com.ecommerce.project.dto.*;
import com.ecommerce.project.models.*;
import com.ecommerce.project.repositories.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.*;

@Service
public class AuthService {
    private final UserRepository users;
    private final OAuthAccountRepository oauthAccounts;
    private final RefreshTokenRepository refreshTokens;
    private final PasswordEncoder passwords;
    private final JwtService jwt;
    private final AuthProperties properties;
    private final SecureRandom random = new SecureRandom();

    public AuthService(UserRepository users, OAuthAccountRepository oauthAccounts, RefreshTokenRepository refreshTokens, PasswordEncoder passwords, JwtService jwt, AuthProperties properties) {
        this.users = users;
        this.oauthAccounts = oauthAccounts;
        this.refreshTokens = refreshTokens;
        this.passwords = passwords;
        this.jwt = jwt;
        this.properties = properties;
    }

    @Transactional
    public LoginResult register(RegisterRequest request, String device) {
        String email = normalize(request.email());
        if (users.findByEmail(email).isPresent())
            throw new ResponseStatusException(HttpStatus.CONFLICT, "An account already exists for this email");
        User user = new User();
        user.setEmail(email);
        user.setDisplayName(request.displayName().trim());
        user.setPasswordHash(passwords.encode(request.password()));
        user = users.save(user);
        String refresh = issueRefreshToken(user, UUID.randomUUID().toString(), device);
        return new LoginResult(response(user, refresh), refresh);
    }

    @Transactional
    public LoginResult login(LoginRequest request, String device) {
        User user = users.findByEmail(normalize(request.email())).orElseThrow(this::invalidCredentials);
        if (!user.isEnabled() || user.getPasswordHash() == null || !passwords.matches(request.password(), user.getPasswordHash()))
            throw invalidCredentials();
        String refresh = issueRefreshToken(user, UUID.randomUUID().toString(), device);
        return new LoginResult(response(user, refresh), refresh);
    }

    @Transactional
    public LoginResult refresh(String rawToken, String device) {
        RefreshToken token = refreshTokens.findByTokenHash(hash(rawToken)).orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid refresh token"));
        if (token.isRevoked()) {
            token.getUser().incrementTokenVersion();
            refreshTokens.revokeActiveByUser(token.getUser());
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh token reuse detected");
        }
        if (token.getExpiresAt().isBefore(Instant.now())) {
            token.setRevoked(true);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Refresh token expired");
        }
        token.setRevoked(true);
        String replacement = issueRefreshToken(token.getUser(), token.getFamilyId(), device);
        return new LoginResult(response(token.getUser(), replacement), replacement);
    }

    @Transactional
    public void logout(String rawToken) {
        if (rawToken != null) refreshTokens.findByTokenHash(hash(rawToken)).ifPresent(t -> t.setRevoked(true));
    }

    @Transactional
    public User oauthGoogleUser(String subject, String email, String name) {
        return oauthAccounts.findByProviderAndProviderSubject(AuthProvider.GOOGLE, subject).map(OAuthAccount::getUser).orElseGet(() -> {
            String normalized = normalize(email);
            User user = users.findByEmail(normalized).orElseGet(() -> {
                User created = new User();
                created.setEmail(normalized);
                created.setDisplayName(name);
                return users.save(created);
            });
            OAuthAccount account = new OAuthAccount();
            account.setProvider(AuthProvider.GOOGLE);
            account.setProviderSubject(subject);
            account.setUser(user);
            oauthAccounts.save(account);
            return user;
        });
    }

    @Transactional
    public String issueForOauth(User user, String device) {
        return issueRefreshToken(user, UUID.randomUUID().toString(), device);
    }

    public AuthResponse accessFor(User user) {
        return response(user, null);
    }

    public User findUser(String email) {
        return users.findByEmail(email).orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required"));
    }

    private AuthResponse response(User user, String refresh) {
        return new AuthResponse(jwt.createAccessToken(user), "Bearer", jwt.expiresInSeconds(), new UserResponse(user.getId(), user.getEmail(), user.getDisplayName(), List.of("ROLE_" + user.getRole())));
    }

    private String issueRefreshToken(User user, String family, String device) {
        String raw = randomToken();
        RefreshToken token = new RefreshToken();
        token.setUser(user);
        token.setFamilyId(family);
        token.setTokenHash(hash(raw));
        token.setExpiresAt(Instant.now().plusSeconds(properties.refreshTokenDays() * 86400));
        token.setDeviceInfo(device);
        refreshTokens.save(token);
        return raw;
    }

    private String randomToken() {
        byte[] bytes = new byte[48];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hash(String value) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException(ex);
        }
    }

    private String normalize(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private ResponseStatusException invalidCredentials() {
        return new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password");
    }
}
