package com.ecommerce.project.service;

import com.ecommerce.project.config.JwtProperties;
import com.ecommerce.project.models.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;
import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;

@Service
public class JwtService {
    private final JwtProperties properties;
    private final SecretKey key;
    public JwtService(JwtProperties properties) {
        this.properties = properties;
        this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(properties.secret()));
    }
    public String createAccessToken(User user) {
        Instant now = Instant.now();
        return Jwts.builder().subject(user.getEmail()).issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(properties.accessTokenMinutes() * 60)))
                .claim("roles", java.util.List.of("ROLE_" + user.getRole().name()))
                .claim("tv", user.getTokenVersion()).issuer("ecommerce-api").signWith(key).compact();
    }
    public Claims parse(String token) { return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload(); }
    public long expiresInSeconds() { return properties.accessTokenMinutes() * 60; }
}
