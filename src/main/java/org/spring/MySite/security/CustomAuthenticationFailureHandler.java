package org.spring.MySite.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;


import org.spring.MySite.services.LoginAttemptService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.security.web.WebAttributes;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.LocaleResolver;


import java.io.IOException;
import java.util.Locale;


@Component
public class CustomAuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {
        //implements AuthenticationFailureHandler

    @Autowired
    private HttpServletRequest request;
    @Autowired
    private LoginAttemptService loginAttemptService;
   @Autowired
    private MessageSource messages;
   @Autowired
    private LocaleResolver localeResolver;


    /*@Override
    public void onAuthenticationFailure(
            HttpServletRequest httpServletRequest,
            HttpServletResponse httpServletResponse,
            AuthenticationException e) throws IOException {

        //httpServletResponse.setHeader("failed", LocalDateTime.now().toString());
        httpServletResponse.sendRedirect("/userIsAbsent");
    }*/

    @Override
    public void onAuthenticationFailure(
            final HttpServletRequest request, final HttpServletResponse response,
            final AuthenticationException exception) throws IOException, ServletException {


        //String errorMessage = messages.getMessage("message.badCredentials", null, locale);
        if (exception instanceof UsernameNotFoundException || exception instanceof BadCredentialsException) {
            response.sendRedirect("/userIsAbsent");
        } else if (exception instanceof LockedException) {
            response.sendRedirect("/blockedUser");
        } else if (exception.getMessage() != null && exception.getMessage().equalsIgnoreCase("blocked")) {

            setDefaultFailureUrl("/login?error"); //setDefaultFailureUrl("/login?error=true");
            super.onAuthenticationFailure(request, response, exception);

            final Locale locale = localeResolver.resolveLocale(request);
            String errorMessage = messages.getMessage("auth.message.blocked", null, locale);

            request.getSession()
                    .setAttribute(WebAttributes.AUTHENTICATION_EXCEPTION, errorMessage);
        }
    }
}
