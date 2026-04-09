package com.example.sdkdemo.filter;

import com.example.sdkdemo.service.RateLimitService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
@Slf4j
public class RateLimitingFilter implements Filter {
    
    private final RateLimitService rateLimitService;
    
    public RateLimitingFilter(RateLimitService rateLimitService) {
        this.rateLimitService = rateLimitService;
    }
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) 
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        String requestURI = httpRequest.getRequestURI();
        String method = httpRequest.getMethod();
        
        // Only apply rate limiting to API endpoints
        if (requestURI.startsWith("/api/")) {
            if (!rateLimitService.isAllowed(httpRequest)) {
                log.warn("Rate limit exceeded for {} {}", method, requestURI);
                httpResponse.setStatus(HttpServletResponse.SC_TOO_MANY_REQUESTS);
                httpResponse.setContentType("application/json");
                
                String errorResponse = "{\"error\":\"Rate limit exceeded. Please try again later.\"}";
                httpResponse.getWriter().write(errorResponse);
                httpResponse.getWriter().flush();
                return;
            }
        }
        
        chain.doFilter(request, response);
    }
}