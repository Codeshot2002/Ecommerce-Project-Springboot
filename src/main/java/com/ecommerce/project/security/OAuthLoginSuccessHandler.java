package com.ecommerce.project.security;

import com.ecommerce.project.config.AuthProperties;
import com.ecommerce.project.models.User;
import com.ecommerce.project.service.AuthService;
import jakarta.servlet.http.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Component
public class OAuthLoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    private final AuthService auth;
    private final AuthProperties properties;
    private final String frontendOrigin;

    public OAuthLoginSuccessHandler(AuthService auth, AuthProperties properties, @Value("${app.frontend-origin}") String frontendOrigin) {
        this.auth = auth;
        this.properties = properties;
        this.frontendOrigin = frontendOrigin;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws java.io.IOException, jakarta.servlet.ServletException {
        OAuth2User principal = (OAuth2User) authentication.getPrincipal();
        Object verified = principal.getAttribute("email_verified");
        String email = principal.getAttribute("email");
        String subject = principal.getAttribute("sub");
        if (email == null || subject == null || !Boolean.TRUE.equals(verified)) {
            response.sendError(401, "Google account email is not verified");
            return;
        }
        User user = auth.oauthGoogleUser(subject, email, principal.getAttribute("name"));
        Cookie cookie = new Cookie("refresh_token", auth.issueForOauth(user, request.getHeader("User-Agent")));
        cookie.setHttpOnly(true);
        cookie.setSecure(properties.cookieSecure());
        cookie.setPath("/api/auth");
        cookie.setMaxAge((int) (properties.refreshTokenDays() * 86400));
        cookie.setAttribute("SameSite", "Lax");
        response.addCookie(cookie);
        getRedirectStrategy().sendRedirect(request, response, frontendOrigin + "/auth/callback");
    }
}
