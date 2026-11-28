package com.example.sdkdemo.service;

import com.example.sdkdemo.exception.InsufficientInventoryException;
import com.example.sdkdemo.exception.ResourceNotFoundException;
import com.example.sdkdemo.model.Product;
import com.example.sdkdemo.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryService {

    private final ProductRepository productRepository;

    @Transactional
    public Product reserveStock(String sku, int quantity) {
        Product product = productRepository.findBySku(sku)
                .orElseThrow(() -> new ResourceNotFoundException("Product", sku));
        int available = product.getAvailableQuantity();
        if (available < quantity) {
            throw new InsufficientInventoryException(sku, quantity, available);
        }
        product.setReservedQuantity(product.getReservedQuantity() + quantity);
        return productRepository.save(product);
    }

    @Transactional
    public void commitReservation(Long productId, int quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", productId));
        product.setStockQuantity(Math.max(0, product.getStockQuantity() - quantity));
        product.setReservedQuantity(Math.max(0, product.getReservedQuantity() - quantity));
        productRepository.save(product);
    }

    public List<Product> getLowStockAlerts(int threshold) {
        return productRepository.findLowStockProducts(threshold);
    }

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }
}
