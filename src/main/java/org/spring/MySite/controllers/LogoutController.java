package org.spring.MySite.controllers;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Controller
public class LogoutController {

    private final ClientRegistrationRepository clientRegistrationRepository;

    public LogoutController(ClientRegistrationRepository clientRegistrationRepository) {
        this.clientRegistrationRepository = clientRegistrationRepository;
    }

    @GetMapping("/oauth2/logout")
    public String logoutOAuth2(HttpServletRequest request, HttpServletResponse response, @Value("${app.base-url}") String baseUrl) throws IOException {
        // 1. Выход из приложения
        new SecurityContextLogoutHandler().logout(request, response,
                SecurityContextHolder.getContext().getAuthentication());

        // 2. Выход из GitHub
        /*ClientRegistration githubRegistration = clientRegistrationRepository.findByRegistrationId("github");
        if (githubRegistration != null) {
            // GitHub использует нестандартный endpoint для выхода
            String logoutUrl = "https://github.com/logout";
            String clientId = githubRegistration.getClientId();
            String returnTo = URLEncoder.encode(baseUrl + "/", StandardCharsets.UTF_8);

            return "redirect:" + logoutUrl + "?client_id=" + clientId + "&returnTo=" + returnTo; //не перенаправляет на returnTo
        }*/
        request.getSession().invalidate();
        return "redirect:/";
    }
}