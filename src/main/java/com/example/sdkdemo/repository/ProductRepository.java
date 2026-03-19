package com.example.sdkdemo.repository;

import com.example.sdkdemo.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    
    List<Product> findByCategory(String category);
    
    Optional<Product> findByName(String name);
    
    @Query("SELECT p FROM Product p WHERE p.stockQuantity < :threshold")
    List<Product> findLowStockProducts(@Param("threshold") int threshold);
    
    @Query("SELECT p FROM Product p WHERE p.price BETWEEN :min AND :max")
    List<Product> findByPriceRange(@Param("min") java.math.BigDecimal min, 
                                   @Param("max") java.math.BigDecimal max);
    
    // Simulate slow database query for latency testing
    @Query(value = "SELECT * FROM products p WHERE p.name LIKE ?1 ORDER BY RAND() LIMIT ?2", 
           nativeQuery = true)
    List<Product> findRandomProducts(String pattern, int limit);
}
