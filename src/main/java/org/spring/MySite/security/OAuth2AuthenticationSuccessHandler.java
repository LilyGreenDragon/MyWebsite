package org.spring.MySite.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.session.FindByIndexNameSessionRepository;
import org.springframework.session.Session;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;

@Component
public class OAuth2AuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    @Autowired
    private FindByIndexNameSessionRepository<? extends Session> sessionRepository;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        //Ограничение колличества сессий .maximumSessions(1)
        OAuth2User oauthUser = (OAuth2User) authentication.getPrincipal();
        String username = oauthUser.getAttribute("login");
        // String username = authentication.getName();
        String currentSessionId = request.getSession().getId();

        Map<String, ? extends Session> sessions = sessionRepository.findByPrincipalName(username);

        for (Session session : sessions.values()) {
            if (!session.getId().equals(currentSessionId)) {
                sessionRepository.deleteById(session.getId()); // Удаляем только старые
            }
        }

        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        for (GrantedAuthority grantedAuthority : authorities) {
            if (grantedAuthority.getAuthority().equals("ADMIN")) {
                response.sendRedirect("/admin/admin");
            }
            else if (grantedAuthority.getAuthority().equals("newOAuth2")) {
                response.sendRedirect("/oauth2/password");
            }
               else { response.sendRedirect("/");}

        }
    }
}