package com.example.sdkdemo.config;

import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.time.Duration;

@Configuration
public class RateLimitConfig {

    @Bean
    public RateLimiterRegistry rateLimiterRegistry() {
        RateLimiterConfig config = RateLimiterConfig.custom()
                .limitForPeriod(10) // 10 requests per period
                .limitRefreshPeriod(Duration.ofSeconds(60)) // per 60 seconds
                .timeoutDuration(Duration.ofMillis(100))
                .build();
        
        return RateLimiterRegistry.of(config);
    }
}