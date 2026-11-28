package com.example.sdkdemo.controller;

import com.example.sdkdemo.dto.ApiResponse;
import com.example.sdkdemo.model.Product;
import com.example.sdkdemo.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @GetMapping("/products")
    public ResponseEntity<ApiResponse<List<Product>>> listProducts() {
        return ResponseEntity.ok(ApiResponse.success(inventoryService.getAllProducts()));
    }

    @GetMapping("/low-stock")
    public ResponseEntity<ApiResponse<List<Product>>> getLowStock(@RequestParam(defaultValue = "10") int threshold) {
        return ResponseEntity.ok(ApiResponse.success(inventoryService.getLowStockAlerts(threshold)));
    }
}
