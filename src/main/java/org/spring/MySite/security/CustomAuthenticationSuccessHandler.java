package org.spring.MySite.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest httpServletRequest,
            HttpServletResponse httpServletResponse,
            Authentication authentication)

            throws IOException {

        var authorities = authentication.getAuthorities();

        var auth =
                authorities.stream()
                        .filter(a -> a.getAuthority().equals("ADMIN"))
                        .findFirst();
        if (auth.isPresent()) {
            httpServletResponse.sendRedirect("/admin/admin");
        } else {
                    httpServletResponse.sendRedirect("/");
        }
    }
}
