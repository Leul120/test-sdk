package com.example.sdkdemo.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableCaching
public class CacheConfig {
    
    @Bean
    public CacheManager cacheManager() {
        // Using simple cache manager for demo - can cause cache performance issues
        ConcurrentMapCacheManager cacheManager = new ConcurrentMapCacheManager(
            "products", "orders"
        );
        
        // Configure to simulate cache issues
        cacheManager.setAllowNullValues(false);
        
        return cacheManager;
    }
}
