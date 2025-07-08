package org.spring.MySite.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Component
public class OAuth2AuthenticationFailureHandler implements AuthenticationFailureHandler {


    private static final Map<String, String> ERROR_MAPPINGS = Map.of(
            "no_verified_email", "You must have at least one verified email",
            "username_is_taken", "This username is already taken",
            "user_blocked", "Too many attempts. Please try again later"
    );

    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception) throws IOException {
        String errorKey = ((OAuth2AuthenticationException) exception).getError().getErrorCode();
        String userMessage = ERROR_MAPPINGS.getOrDefault(errorKey, "Authorization error");

        response.sendRedirect("/login?error=" +
                URLEncoder.encode(userMessage, StandardCharsets.UTF_8));
    }
}


