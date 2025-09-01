package io.github.dumidev.belanova.gateway.gateway.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
@Component
public class LoggingFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // Initialization if needed
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        long startTime = System.currentTimeMillis();

        log.debug("Incoming request: {} {}", httpRequest.getMethod(), httpRequest.getRequestURI());

        try {
            chain.doFilter(request, response);
            long duration = System.currentTimeMillis() - startTime;

            log.debug("Request completed: {} {} - Status: {} - Duration: {}ms",
                     httpRequest.getMethod(), httpRequest.getRequestURI(),
                     httpResponse.getStatus(), duration);

        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("Request failed: {} {} - Duration: {}ms",
                     httpRequest.getMethod(), httpRequest.getRequestURI(), duration, e);
            throw e;
        }
    }

    @Override
    public void destroy() {
        // Cleanup if needed
    }
}