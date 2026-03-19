package com.example.sdkdemo.service;

import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@Slf4j
public class FailureInjectionService {
    
    private final AtomicInteger errorCount = new AtomicInteger(0);
    private final AtomicInteger requestCount = new AtomicInteger(0);
    
    // Simulate random failures based on probability
    public boolean shouldFail(double probability) {
        return ThreadLocalRandom.current().nextDouble() < probability;
    }
    
    // Simulate random latency
    public long simulateLatency(int minMs, int maxMs) {
        return ThreadLocalRandom.current().nextLong(minMs, maxMs + 1);
    }
    
    // Track error count for threshold testing
    public void incrementErrorCount() {
        int count = errorCount.incrementAndGet();
        log.warn("Error count incremented to: {}", count);
    }
    
    public void resetErrorCount() {
        errorCount.set(0);
        log.info("Error count reset");
    }
    
    public int getErrorCount() {
        return errorCount.get();
    }
    
    // Track request count for throughput testing
    public void incrementRequestCount() {
        requestCount.incrementAndGet();
    }
    
    public int getRequestCount() {
        return requestCount.get();
    }
    
    // Simulate memory pressure
    public byte[] allocateMemory(int sizeMB) {
        log.warn("Allocating {} MB of memory", sizeMB);
        return new byte[sizeMB * 1024 * 1024];
    }
    
    // Simulate CPU pressure
    public void consumeCPU(int iterations) {
        log.warn("Starting CPU intensive task with {} iterations", iterations);
        long result = 0;
        for (int i = 0; i < iterations; i++) {
            result += Math.pow(Math.random(), 2);
        }
        log.debug("CPU task completed, result: {}", result);
    }
    
    // Simulate security vulnerability patterns
    public String injectSecurityPattern(String input) {
        if (input.toLowerCase().contains("<script>")) {
            log.error("SECURITY_SIGNAL: XSS pattern detected in input: {}", input);
            return input;
        }
        if (input.toLowerCase().matches(".*('|(\\-\\-)|(;)|(\\||\\|)|(\\*|\\*)).*")) {
            log.error("SECURITY_SIGNAL: SQL injection pattern detected in input: {}", input);
            return input;
        }
        return input;
    }
}
