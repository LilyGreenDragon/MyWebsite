package org.spring.MySite.security;


import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

public class UsernamePasswordAuthFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

    if ("POST".equalsIgnoreCase(httpRequest.getMethod())) {
        String username = httpRequest.getParameter("username");
        String password = httpRequest.getParameter("password");

        if (username != null && password != null) {
            if (username.length() > 50 || password.length() > 50) {
                String redirectPath = httpRequest.getContextPath() + "/userIsAbsent";
                httpResponse.sendRedirect(redirectPath);
                return;
            }
        }
    }

        filterChain.doFilter(request, response);
}

}
