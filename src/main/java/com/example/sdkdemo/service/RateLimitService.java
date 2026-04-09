package com.example.sdkdemo.service;

import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import jakarta.servlet.http.HttpServletRequest;

@Service
@Slf4j
public class RateLimitService {
    
    private final RateLimiterRegistry rateLimiterRegistry;
    
    public RateLimitService(RateLimiterRegistry rateLimiterRegistry) {
        this.rateLimiterRegistry = rateLimiterRegistry;
    }
    
    public boolean isAllowed(HttpServletRequest request) {
        String key = getClientIdentifier(request);
        RateLimiter rateLimiter = rateLimiterRegistry.rateLimiter(key);
        
        try {
            RateLimiter.Response response = rateLimiter.acquirePermission();
            return response.isPermissionGranted();
        } catch (Exception e) {
            log.error("Error checking rate limit for client: {}", key, e);
            return false;
        }
    }
    
    private String getClientIdentifier(HttpServletRequest request) {
        String apiKey = request.getHeader("X-API-Key");
        if (apiKey != null && !apiKey.trim().isEmpty()) {
            return "api_key:" + apiKey;
        }
        
        String ipAddress = getClientIP(request);
        return "ip:" + ipAddress;
    }
    
    private String getClientIP(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader != null) {
            return xfHeader.split(",")[0];
        }
        return request.getRemoteAddr();
    }
}