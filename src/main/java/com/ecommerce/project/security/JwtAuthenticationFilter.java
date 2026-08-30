package com.ecommerce.project.security;

import com.ecommerce.project.models.User;
import com.ecommerce.project.service.AuthService;
import com.ecommerce.project.service.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtService jwt; private final AuthService auth;
    public JwtAuthenticationFilter(JwtService jwt, AuthService auth) { this.jwt=jwt; this.auth=auth; }
    @Override protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        String header=request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ") && SecurityContextHolder.getContext().getAuthentication()==null) {
            try {
                Claims claims=jwt.parse(header.substring(7)); User user=auth.findUser(claims.getSubject());
                Number tv=claims.get("tv", Number.class);
                if (!user.isEnabled() || tv == null || tv.longValue()!=user.getTokenVersion()) throw new JwtException("Token revoked");
                List<SimpleGrantedAuthority> authorities=((List<?>)claims.get("roles", List.class)).stream().map(Object::toString).map(SimpleGrantedAuthority::new).toList();
                SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(user.getEmail(), null, authorities));
            } catch (JwtException | IllegalArgumentException ignored) { SecurityContextHolder.clearContext(); }
        }
        chain.doFilter(request,response);
    }
}
