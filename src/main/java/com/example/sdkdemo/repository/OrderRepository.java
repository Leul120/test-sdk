package com.example.sdkdemo.repository;

import com.example.sdkdemo.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    
    Optional<Order> findByOrderNumber(String orderNumber);
    
    List<Order> findByCustomerEmail(String customerEmail);
    
    List<Order> findByStatus(Order.OrderStatus status);
    
    @Query("SELECT o FROM Order o WHERE o.createdAt BETWEEN :start AND :end")
    List<Order> findOrdersByDateRange(@Param("start") LocalDateTime start, 
                                      @Param("end") LocalDateTime end);
    
    @Query("SELECT COUNT(o) FROM Order o WHERE o.status = :status AND o.createdAt > :since")
    Long countOrdersByStatusSince(@Param("status") Order.OrderStatus status, 
                                  @Param("since") LocalDateTime since);
    
    // Simulate complex query that can cause timeouts
    @Query(value = "SELECT o.* FROM orders o " +
                   "JOIN order_items oi ON o.id = oi.order_id " +
                   "JOIN products p ON oi.product_id = p.id " +
                   "WHERE o.created_at > ?1 " +
                   "GROUP BY o.id " +
                   "HAVING SUM(oi.quantity * oi.unit_price) > ?2 " +
                   "ORDER BY o.created_at DESC " +
                   "LIMIT ?3", nativeQuery = true)
    List<Order> findComplexOrderQuery(LocalDateTime since, java.math.BigDecimal minTotal, int limit);
}
