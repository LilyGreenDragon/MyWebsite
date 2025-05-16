package org.spring.MySite.security;

import jakarta.servlet.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;


public class RequestLoggerFilter implements Filter {
    private static Logger logger = LogManager.getLogger(RequestLoggerFilter.class);


    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain)
            throws IOException, ServletException {

        // print message
        logger.info("Request received");
        // call next filter in chain
        filterChain.doFilter(request, response);
    }
}
