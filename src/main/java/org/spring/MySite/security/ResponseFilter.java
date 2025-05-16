package org.spring.MySite.security;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;

public class ResponseFilter implements Filter {
    private static Logger logger = LogManager.getLogger(RequestLoggerFilter.class);


    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain)
            throws IOException, ServletException {
        HttpServletResponse httpServletResponse = (HttpServletResponse) response;
        httpServletResponse.setHeader("Access-Control-Allow-Origin", "http://localhost:8080");
        httpServletResponse.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE");

// Specify the allowed headers
        httpServletResponse.setHeader("Access-Control-Allow-Headers", "*");
// Enable support for credentials (e.g., cookies)
        httpServletResponse.setHeader("Access-Control-Allow-Credentials", "true");

        // print message
        logger.info("Header added to response");
        // call next filter in chain
        filterChain.doFilter(request, response);
    }
}
