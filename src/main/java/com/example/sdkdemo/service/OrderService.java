package com.example.sdkdemo.service;

import com.example.sdkdemo.dto.CreateOrderRequest;
import com.example.sdkdemo.dto.OrderItemRequest;
import com.example.sdkdemo.dto.OrderResponse;
import com.example.sdkdemo.exception.ResourceNotFoundException;
import com.example.sdkdemo.model.*;
import com.example.sdkdemo.repository.OrderRepository;
import com.example.sdkdemo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final InventoryService inventoryService;
    private final PricingService pricingService;
    private final BillingService billingService;
    private final AuditService auditService;
    private final NotificationService notificationService;

    @Transactional
    public OrderResponse placeOrder(CreateOrderRequest request) {
        User buyer = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User", request.getUserId()));

        Order order = Order.builder()
                .orderNumber("ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .userId(buyer.getId())
                .departmentId(request.getDepartmentId())
                .status("PENDING")
                .discountPercent(request.getDiscountPercent())
                .shippingRegion(request.getShippingRegion())
                .build();

        List<String> reservedSkus = new ArrayList<>();
        for (OrderItemRequest itemReq : request.getItems()) {
            Product product = inventoryService.reserveStock(itemReq.getSku(), itemReq.getQuantity());
            BigDecimal lineTotal = product.getUnitPrice().multiply(BigDecimal.valueOf(itemReq.getQuantity()));
            OrderItem item = OrderItem.builder()
                    .order(order)
                    .productId(product.getId())
                    .sku(product.getSku())
                    .productName(product.getName())
                    .quantity(itemReq.getQuantity())
                    .unitPrice(product.getUnitPrice())
                    .lineTotal(lineTotal)
                    .build();
            order.getItems().add(item);
            reservedSkus.add(product.getSku());
        }

        BigDecimal subtotal = pricingService.calculateSubtotal(order.getItems());
        order.setSubtotal(subtotal);
        BigDecimal discounted = pricingService.applyDiscounts(subtotal, request.getDiscountPercent(), request.getPromoCode());
        order.setTotalAmount(discounted);
        order.setStatus("CONFIRMED");

        Order savedOrder = orderRepository.save(order);
        Invoice invoice = billingService.issueInvoiceForOrder(savedOrder, discounted);

        auditService.record("ORDER", savedOrder.getOrderNumber(), "CONFIRMED", buyer.getEmail(),
                "Items=" + reservedSkus.size() + ", Total=" + invoice.getTotalAmount());
        notificationService.notifyOrderConfirmed(buyer.getEmail(), savedOrder.getOrderNumber(),
                invoice.getTotalAmount().toPlainString());

        return OrderResponse.builder()
                .orderId(savedOrder.getId())
                .orderNumber(savedOrder.getOrderNumber())
                .status(savedOrder.getStatus())
                .subtotal(subtotal)
                .taxAmount(invoice.getTaxAmount())
                .totalAmount(invoice.getTotalAmount())
                .invoiceNumber(invoice.getInvoiceNumber())
                .reservedSkus(reservedSkus)
                .build();
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    public Order getOrderByNumber(String orderNumber) {
        return orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Order", orderNumber));
    }
}
