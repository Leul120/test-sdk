package com.example.sdkdemo.controller;

import com.example.sdkdemo.model.Product;
import com.example.sdkdemo.model.Order;
import com.example.sdkdemo.dto.OrderRequest;
import com.example.sdkdemo.dto.SearchRequest;
import com.example.sdkdemo.service.ProductService;
import com.example.sdkdemo.service.OrderService;
import com.example.sdkdemo.service.FailureInjectionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/ecommerce")
@RequiredArgsConstructor
@Slf4j
public class EcommerceController {
    
    private final ProductService productService;
    private final OrderService orderService;
    private final FailureInjectionService failureService;
    
    // Product endpoints
    @GetMapping("/products")
    public ResponseEntity<List<Product>> getAllProducts() {
        failureService.incrementRequestCount();
        
        // Simulate occasional latency spikes
        if (failureService.shouldFail(0.1)) { // 10% chance
            long latency = failureService.simulateLatency(1000, 3000);
            try {
                Thread.sleep(latency);
                log.warn("Simulated latency spike: {}ms", latency);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        
        return ResponseEntity.ok(productService.getAllProducts());
    }
    
    @GetMapping("/products/{id}")
    public ResponseEntity<Product> getProduct(@PathVariable Long id) {
        failureService.incrementRequestCount();
        
        try {
            Product product = productService.getProduct(id);
            return ResponseEntity.ok(product);
        } catch (RuntimeException e) {
            failureService.incrementErrorCount();
            log.error("Error fetching product: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }
    
    @PostMapping("/products/search")
    public ResponseEntity<List<Product>> searchProducts(@Valid @RequestBody SearchRequest request) {
        failureService.incrementRequestCount();
        
        // Check for security patterns
        String sanitizedQuery = failureService.injectSecurityPattern(request.getQuery());
        
        try {
            List<Product> products = productService.searchProducts(sanitizedQuery);
            return ResponseEntity.ok(products);
        } catch (Exception e) {
            failureService.incrementErrorCount();
            log.error("Search error: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    // Order endpoints
    @PostMapping("/orders")
    public ResponseEntity<Order> createOrder(@Valid @RequestBody OrderRequest orderRequest) {
        failureService.incrementRequestCount();
        
        // Simulate random failures (5% chance)
        if (failureService.shouldFail(0.05)) {
            failureService.incrementErrorCount();
            log.error("Simulated order creation failure");
            return ResponseEntity.status(500).build();
        }
        
        try {
            Order order = orderService.createOrder(orderRequest);
            return ResponseEntity.ok(order);
        } catch (Exception e) {
            failureService.incrementErrorCount();
            log.error("Order creation error: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping("/orders/{orderNumber}")
    public ResponseEntity<Order> getOrder(@PathVariable String orderNumber) {
        failureService.incrementRequestCount();
        
        // Simulate database timeout (3% chance)
        if (failureService.shouldFail(0.03)) {
            failureService.incrementErrorCount();
            log.error("Database connection timeout simulated");
            return ResponseEntity.status(503).header("Retry-After", "5").build();
        }
        
        try {
            Order order = orderService.getOrderByNumber(orderNumber);
            return ResponseEntity.ok(order);
        } catch (RuntimeException e) {
            failureService.incrementErrorCount();
            log.error("Error fetching order: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }
    
    // Stress testing endpoints
    @PostMapping("/stress/memory")
    public ResponseEntity<Map<String, Object>> memoryStress(@RequestParam int sizeMB) {
        failureService.incrementRequestCount();
        
        try {
            byte[] memory = failureService.allocateMemory(sizeMB);
            return ResponseEntity.ok(Map.of(
                "allocated", sizeMB + "MB",
                "status", "success",
                "timestamp", System.currentTimeMillis()
            ));
        } catch (OutOfMemoryError e) {
            failureService.incrementErrorCount();
            log.error("MEMORY_PRESSURE: OutOfMemoryError triggered");
            return ResponseEntity.status(500).body(Map.of(
                "error", "Memory allocation failed",
                "requested", sizeMB + "MB"
            ));
        }
    }
    
    @PostMapping("/stress/cpu")
    public ResponseEntity<Map<String, Object>> cpuStress(@RequestParam int iterations) {
        failureService.incrementRequestCount();
        
        CompletableFuture.runAsync(() -> {
            failureService.consumeCPU(iterations);
        });
        
        return ResponseEntity.ok(Map.of(
            "iterations", iterations,
            "status", "CPU stress started",
            "timestamp", System.currentTimeMillis()
        ));
    }
    
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        return ResponseEntity.ok(Map.of(
            "status", "healthy",
            "errors", failureService.getErrorCount(),
            "requests", failureService.getRequestCount(),
            "timestamp", System.currentTimeMillis()
        ));
    }
}
