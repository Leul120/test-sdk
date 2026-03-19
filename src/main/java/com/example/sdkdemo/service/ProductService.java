package com.example.sdkdemo.service;

import com.example.sdkdemo.model.Product;
import com.example.sdkdemo.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {
    
    private final ProductRepository productRepository;
    private final FailureInjectionService failureService;
    
    @Cacheable(value = "products", key = "#root.methodName")
    public List<Product> getAllProducts() {
        log.info("Fetching all products from database");
        
        // Simulate occasional slow query
        if (failureService.shouldFail(0.05)) { // 5% chance
            try {
                Thread.sleep(failureService.simulateLatency(500, 1500));
                log.warn("Slow product query simulated");
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        
        return productRepository.findAll();
    }
    
    @Cacheable(value = "products", key = "#id")
    public Product getProduct(Long id) {
        log.info("Fetching product with id: {}", id);
        
        // Simulate cache miss
        if (failureService.shouldFail(0.3)) { // 30% chance
            log.warn("Cache miss simulated for product {}", id);
        }
        
        return productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
    }
    
    @Transactional
    @CacheEvict(value = "products", allEntries = true)
    public Product createProduct(Product product) {
        log.info("Creating new product: {}", product.getName());
        
        // Simulate potential database deadlock
        if (failureService.shouldFail(0.02)) { // 2% chance
            throw new RuntimeException("Deadlock detected when creating product");
        }
        
        return productRepository.save(product);
    }
    
    @Transactional
    @CacheEvict(value = "products", key = "#id")
    public Product updateProduct(Long id, Product productDetails) {
        log.info("Updating product with id: {}", id);
        
        Product product = getProduct(id);
        product.setName(productDetails.getName());
        product.setDescription(productDetails.getDescription());
        product.setPrice(productDetails.getPrice());
        product.setStockQuantity(productDetails.getStockQuantity());
        product.setCategory(productDetails.getCategory());
        
        return productRepository.save(product);
    }
    
    @Transactional
    @CacheEvict(value = "products", key = "#id")
    public void deleteProduct(Long id) {
        log.info("Deleting product with id: {}", id);
        productRepository.deleteById(id);
    }
    
    public List<Product> searchProducts(String query) {
        log.info("Searching products with query: {}", query);
        
        // Simulate SQL injection vulnerability
        if (query.toLowerCase().contains("drop") || query.toLowerCase().contains("delete")) {
            log.error("Potential SQL injection in search query: {}", query);
        }
        
        // Simulate poor cache performance
        if (failureService.shouldFail(0.4)) { // 40% chance
            log.warn("Cache miss in product search - performance degradation");
        }
        
        return productRepository.findRandomProducts("%" + query + "%", 50);
    }
    
    public List<Product> getLowStockProducts() {
        log.info("Fetching low stock products");
        return productRepository.findLowStockProducts(10);
    }
    
    public List<Product> getProductsByCategory(String category) {
        log.info("Fetching products in category: {}", category);
        return productRepository.findByCategory(category);
    }
}
