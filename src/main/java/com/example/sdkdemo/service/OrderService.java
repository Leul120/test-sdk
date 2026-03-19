package com.example.sdkdemo.service;

import com.example.sdkdemo.model.Order;
import com.example.sdkdemo.model.OrderItem;
import com.example.sdkdemo.model.Product;
import com.example.sdkdemo.dto.OrderRequest;
import com.example.sdkdemo.dto.OrderItemRequest;
import com.example.sdkdemo.repository.OrderRepository;
import com.example.sdkdemo.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {
    
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final FailureInjectionService failureService;
    
    @Transactional
    public Order createOrder(OrderRequest orderRequest) {
        log.info("Creating order for customer: {}", orderRequest.getCustomerEmail());
        
        // Simulate database connection issues
        if (failureService.shouldFail(0.03)) { // 3% chance
            throw new RuntimeException("Connection timeout: Unable to connect to database");
        }
        
        Order order = new Order();
        order.setOrderNumber(generateOrderNumber());
        order.setCustomerEmail(orderRequest.getCustomerEmail());
        order.setStatus(Order.OrderStatus.PENDING);
        
        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;
        
        for (OrderItemRequest itemRequest : orderRequest.getItems()) {
            Product product = productRepository.findById(itemRequest.getProductId())
                    .orElseThrow(() -> new RuntimeException("Product not found: " + itemRequest.getProductId()));
            
            // Check stock availability
            if (product.getStockQuantity() < itemRequest.getQuantity()) {
                throw new RuntimeException("Insufficient stock for product: " + product.getName());
            }
            
            // Update stock
            product.setStockQuantity(product.getStockQuantity() - itemRequest.getQuantity());
            productRepository.save(product);
            
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(product);
            orderItem.setQuantity(itemRequest.getQuantity());
            orderItem.setUnitPrice(product.getPrice());
            orderItem.calculateTotal();
            
            orderItems.add(orderItem);
            totalAmount = totalAmount.add(orderItem.getTotalPrice());
        }
        
        order.setOrderItems(orderItems);
        order.setTotalAmount(totalAmount);
        
        // Simulate potential timeout during order processing
        if (failureService.shouldFail(0.02)) { // 2% chance
            try {
                Thread.sleep(2000); // Simulate slow processing
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        
        Order savedOrder = orderRepository.save(order);
        
        // Process order asynchronously (can fail)
        CompletableFuture.runAsync(() -> {
            try {
                processOrderAsync(savedOrder);
            } catch (Exception e) {
                log.error("Async order processing failed: {}", e.getMessage());
                failureService.incrementErrorCount();
            }
        });
        
        return savedOrder;
    }
    
    private void processOrderAsync(Order order) {
        // Simulate processing delay
        try {
            Thread.sleep(1000);
            
            // Simulate occasional processing failure
            if (failureService.shouldFail(0.05)) { // 5% chance
                order.setStatus(Order.OrderStatus.FAILED);
                log.error("Order processing failed for order: {}", order.getOrderNumber());
            } else {
                order.setStatus(Order.OrderStatus.CONFIRMED);
                log.info("Order processed successfully: {}", order.getOrderNumber());
            }
            
            orderRepository.save(order);
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            order.setStatus(Order.OrderStatus.FAILED);
            orderRepository.save(order);
        }
    }
    
    public Order getOrderByNumber(String orderNumber) {
        log.info("Fetching order with number: {}", orderNumber);
        
        // Simulate database deadlock
        if (failureService.shouldFail(0.01)) { // 1% chance
            throw new RuntimeException("Deadlock detected while fetching order");
        }
        
        return orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderNumber));
    }
    
    public List<Order> getOrdersByCustomer(String customerEmail) {
        log.info("Fetching orders for customer: {}", customerEmail);
        return orderRepository.findByCustomerEmail(customerEmail);
    }
    
    @Transactional
    public Order updateOrderStatus(Long orderId, Order.OrderStatus status) {
        log.info("Updating order {} status to: {}", orderId, status);
        
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));
        
        order.setStatus(status);
        return orderRepository.save(order);
    }
    
    public List<Order> getOrdersByStatus(Order.OrderStatus status) {
        log.info("Fetching orders with status: {}", status);
        return orderRepository.findByStatus(status);
    }
    
    public List<Order> getRecentOrders(int hours) {
        LocalDateTime since = LocalDateTime.now().minusHours(hours);
        return orderRepository.findOrdersByDateRange(since, LocalDateTime.now());
    }
    
    public List<Order> getComplexOrderQuery() {
        log.info("Running complex order query (potential timeout)");
        
        // This query can cause timeouts under load
        return orderRepository.findComplexOrderQuery(
            LocalDateTime.now().minusHours(24),
            BigDecimal.valueOf(100),
            100
        );
    }
    
    private String generateOrderNumber() {
        return "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
