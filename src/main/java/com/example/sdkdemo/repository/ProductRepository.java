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
    Optional<Product> findBySku(String sku);
    List<Product> findByCategoryAndActiveTrue(String category);
    List<Product> findByWarehouseCode(String warehouseCode);

    @Query("SELECT p FROM Product p WHERE (p.stockQuantity - p.reservedQuantity) <= :threshold AND p.active = true")
    List<Product> findLowStockProducts(@Param("threshold") int threshold);
}
