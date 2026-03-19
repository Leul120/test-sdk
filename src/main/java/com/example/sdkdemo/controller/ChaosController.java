package com.example.sdkdemo.controller;

import com.example.sdkdemo.service.FailureInjectionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

@RestController
@RequestMapping("/api/chaos")
@RequiredArgsConstructor
@Slf4j
public class ChaosController {
    
    private final FailureInjectionService failureService;
    private final AtomicInteger connectionCount = new AtomicInteger(0);
    
    // SECURITY_SIGNAL testing
    @PostMapping("/security/xss")
    public ResponseEntity<Map<String, Object>> testXSS(@RequestBody String input) {
        failureService.incrementRequestCount();
        
        String maliciousInput = failureService.injectSecurityPattern(input);
        log.error("SECURITY_SIGNAL: XSS attack attempt - {}", maliciousInput);
        
        return ResponseEntity.ok(Map.of(
            "input", maliciousInput,
            "detected", "XSS pattern",
            "severity", "CRITICAL"
        ));
    }
    
    @PostMapping("/security/sql-injection")
    public ResponseEntity<Map<String, Object>> testSQLInjection(@RequestBody String input) {
        failureService.incrementRequestCount();
        
        String maliciousInput = failureService.injectSecurityPattern(input);
        log.error("SECURITY_SIGNAL: SQL injection attempt - {}", maliciousInput);
        
        return ResponseEntity.ok(Map.of(
            "input", maliciousInput,
            "detected", "SQL injection pattern",
            "severity", "CRITICAL"
        ));
    }
    
    // ERROR_THRESHOLD testing
    @PostMapping("/errors/generate")
    public ResponseEntity<Map<String, Object>> generateErrors(@RequestParam int count) {
        failureService.incrementRequestCount();
        
        for (int i = 0; i < count; i++) {
            failureService.incrementErrorCount();
            log.error("Generated error #{} for threshold testing", i + 1);
        }
        
        return ResponseEntity.ok(Map.of(
            "generated_errors", count,
            "total_errors", failureService.getErrorCount(),
            "threshold_exceeded", failureService.getErrorCount() > 5
        ));
    }
    
    // LATENCY_SPIKE testing
    @PostMapping("/latency/spike")
    public ResponseEntity<Map<String, Object>> createLatencySpike(@RequestParam int durationMs) {
        failureService.incrementRequestCount();
        
        try {
            log.warn("LATENCY_SPIKE: Starting {}ms delay", durationMs);
            Thread.sleep(durationMs);
            return ResponseEntity.ok(Map.of(
                "duration_ms", durationMs,
                "threshold_exceeded", durationMs > 1000,
                "status", "completed"
            ));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return ResponseEntity.status(500).build();
        }
    }
    
    // STATUS_CODE_ISSUES testing
    @GetMapping("/status/{code}")
    public ResponseEntity<Map<String, Object>> testStatusCode(@PathVariable int code) {
        failureService.incrementRequestCount();
        
        if (code >= 400) {
            failureService.incrementErrorCount();
        }
        
        log.warn("STATUS_CODE_ISSUES: Returning status code {}", code);
        
        return ResponseEntity.status(code).body(Map.of(
            "status_code", code,
            "category", code >= 200 && code < 300 ? "success" : "error",
            "timestamp", System.currentTimeMillis()
        ));
    }
    
    // MEMORY_PRESSURE testing
    @PostMapping("/memory/pressure")
    public ResponseEntity<Map<String, Object>> createMemoryPressure(@RequestParam int percentage) {
        failureService.incrementRequestCount();
        
        try {
            Runtime runtime = Runtime.getRuntime();
            long totalMemory = runtime.totalMemory();
            long targetMemory = (long) (totalMemory * (percentage / 100.0));
            long currentMemory = runtime.totalMemory() - runtime.freeMemory();
            long memoryToAllocate = targetMemory - currentMemory;
            
            if (memoryToAllocate > 0) {
                int sizeMB = (int) (memoryToAllocate / (1024 * 1024));
                failureService.allocateMemory(sizeMB);
            }
            
            memoryToAllocate = targetMemory - currentMemory;
            double currentUsage = ((double) (runtime.totalMemory() - runtime.freeMemory()) / runtime.maxMemory()) * 100;
            
            log.warn("MEMORY_PRESSURE: Current usage {}%", currentUsage);
            
            return ResponseEntity.ok(Map.of(
                "target_percentage", percentage,
                "current_usage", currentUsage,
                "threshold_exceeded", currentUsage > 80,
                "allocated_mb", memoryToAllocate > 0 ? memoryToAllocate / (1024 * 1024) : 0
            ));
            
        } catch (OutOfMemoryError e) {
            failureService.incrementErrorCount();
            log.error("MEMORY_PRESSURE: OutOfMemoryError occurred");
            return ResponseEntity.status(500).body(Map.of(
                "error", "OutOfMemoryError",
                "message", "Memory pressure exceeded available memory"
            ));
        }
    }
    
    // CPU_PRESSURE testing
    @PostMapping("/cpu/pressure")
    public ResponseEntity<Map<String, Object>> createCPUPressure(@RequestParam int percentage) {
        failureService.incrementRequestCount();
        
        int iterations = percentage * 1000000; // Scale iterations to percentage
        CompletableFuture.runAsync(() -> {
            failureService.consumeCPU(iterations);
        });
        
        log.warn("CPU_PRESSURE: Started CPU intensive task for {}% usage", percentage);
        
        return ResponseEntity.ok(Map.of(
            "target_percentage", percentage,
            "iterations", iterations,
            "threshold_exceeded", percentage > 75,
            "status", "CPU pressure started"
        ));
    }
    
    // CONNECTION_POOL_EXHAUSTION testing
    @PostMapping("/connections/exhaust")
    public ResponseEntity<Map<String, Object>> exhaustConnections(@RequestParam int count) {
        failureService.incrementRequestCount();
        
        int currentConnections = connectionCount.addAndGet(count);
        double usagePercentage = (currentConnections / 100.0) * 100; // Assuming max 100 connections
        
        log.warn("CONNECTION_POOL_EXHAUSTION: {} active connections ({}%)", 
                currentConnections, usagePercentage);
        
        return ResponseEntity.ok(Map.of(
            "requested_connections", count,
            "active_connections", currentConnections,
            "usage_percentage", usagePercentage,
            "threshold_exceeded", usagePercentage > 90
        ));
    }
    
    @PostMapping("/connections/release")
    public ResponseEntity<Map<String, Object>> releaseConnections(@RequestParam int count) {
        int currentConnections = connectionCount.updateAndGet(val -> Math.max(0, val - count));
        
        return ResponseEntity.ok(Map.of(
            "released_connections", count,
            "active_connections", currentConnections
        ));
    }
    
    // DATABASE_CONNECTION_ISSUES testing
    @PostMapping("/database/timeout")
    public ResponseEntity<Map<String, Object>> simulateDatabaseTimeout() {
        failureService.incrementRequestCount();
        failureService.incrementErrorCount();
        
        log.error("DATABASE_CONNECTION_ISSUES: Connection timeout simulated");
        
        return ResponseEntity.status(503)
                .header("Retry-After", "5")
                .body(Map.of(
                    "error", "Connection timeout",
                    "message", "Database connection timeout after 30 seconds",
                    "retry_after", "5 seconds"
                ));
    }
    
    @PostMapping("/database/deadlock")
    public ResponseEntity<Map<String, Object>> simulateDeadlock() {
        failureService.incrementRequestCount();
        failureService.incrementErrorCount();
        
        log.error("DATABASE_CONNECTION_ISSUES: Deadlock detected");
        
        return ResponseEntity.status(409).body(Map.of(
            "error", "Deadlock detected",
            "message", "Transaction deadlock detected and rolled back"
        ));
    }
    
    // CACHE_PERFORMANCE_ISSUES testing
    @PostMapping("/cache/poor-performance")
    public ResponseEntity<Map<String, Object>> simulateCacheIssues() {
        failureService.incrementRequestCount();
        
        // Simulate poor cache metrics
        double hitRate = Math.random() * 0.4; // < 50%
        double missRate = 1.0 - hitRate;
        int evictions = (int) (Math.random() * 2000); // > 1000
        
        log.warn("CACHE_PERFORMANCE_ISSUES: Hit rate {}%, Miss rate {}%, Evictions {}", 
                hitRate * 100, missRate * 100, evictions);
        
        return ResponseEntity.ok(Map.of(
            "hit_rate", hitRate,
            "miss_rate", missRate,
            "evictions", evictions,
            "hit_rate_threshold_exceeded", hitRate < 0.5,
            "miss_rate_threshold_exceeded", missRate > 0.3,
            "evictions_threshold_exceeded", evictions > 1000
        ));
    }
    
    // TIMEOUT_ISSUES testing
    @PostMapping("/timeout/simulate")
    public ResponseEntity<Map<String, Object>> simulateTimeout(@RequestParam int timeoutMs) {
        failureService.incrementRequestCount();
        
        try {
            Thread.sleep(timeoutMs + 1000); // Exceed timeout
            failureService.incrementErrorCount();
            log.error("TIMEOUT_ISSUES: Operation timed out after {}ms", timeoutMs);
            
            return ResponseEntity.status(408).body(Map.of(
                "error", "Request timeout",
                "timeout_ms", timeoutMs,
                "message", "Operation timed out"
            ));
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return ResponseEntity.status(408).body(Map.of(
                "error", "Request timeout",
                "message", "Operation was interrupted due to timeout"
            ));
        }
    }
    
    // THROUGHPUT_DEGRADATION testing
    @PostMapping("/throughput/degrade")
    public ResponseEntity<Map<String, Object>> degradeThroughput(@RequestParam double rps) {
        failureService.incrementRequestCount();
        
        double currentRPS = Math.random() * 0.5; // Simulate low RPS < 1
        
        log.warn("THROUGHPUT_DEGRADATION: Current RPS {} (target: {})", currentRPS, rps);
        
        return ResponseEntity.ok(Map.of(
            "current_rps", currentRPS,
            "target_rps", rps,
            "threshold_exceeded", currentRPS < 1.0 && failureService.getRequestCount() > 10,
            "total_requests", failureService.getRequestCount()
        ));
    }
    
    // REPEATED_ERROR testing
    @PostMapping("/errors/repeated")
    public ResponseEntity<Map<String, Object>> generateRepeatedErrors(@RequestParam String errorMessage) {
        failureService.incrementRequestCount();
        
        // Generate the same error multiple times
        for (int i = 0; i < 3; i++) {
            failureService.incrementErrorCount();
            log.error("REPEATED_ERROR: {} - Occurrence #{}", errorMessage, i + 1);
        }
        
        return ResponseEntity.ok(Map.of(
            "error_message", errorMessage,
            "occurrences", 3,
            "threshold_exceeded", true,
            "total_errors", failureService.getErrorCount()
        ));
    }
}
