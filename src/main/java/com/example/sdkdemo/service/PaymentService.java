package com.example.sdkdemo.service;

import com.example.sdkdemo.dto.PaymentRequest;
import com.example.sdkdemo.exception.BusinessException;
import com.example.sdkdemo.exception.ResourceNotFoundException;
import com.example.sdkdemo.model.Invoice;
import com.example.sdkdemo.model.Order;
import com.example.sdkdemo.model.OrderItem;
import com.example.sdkdemo.model.PaymentTransaction;
import com.example.sdkdemo.repository.InvoiceRepository;
import com.example.sdkdemo.repository.OrderRepository;
import com.example.sdkdemo.repository.PaymentTransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentTransactionRepository paymentRepository;
    private final InvoiceRepository invoiceRepository;
    private final OrderRepository orderRepository;
    private final InventoryService inventoryService;
    private final AuditService auditService;
    private final NotificationService notificationService;

    @Transactional
    public PaymentTransaction processPayment(PaymentRequest request) {
        Invoice invoice = invoiceRepository.findById(request.getInvoiceId())
                .orElseThrow(() -> new ResourceNotFoundException("Invoice", request.getInvoiceId()));

        if ("PAID".equals(invoice.getStatus())) {
            throw new BusinessException("INVOICE_ALREADY_PAID", "Invoice " + invoice.getInvoiceNumber() + " is already paid");
        }
        if (request.getAmount().compareTo(invoice.getTotalAmount()) < 0) {
            throw new BusinessException("UNDERPAYMENT", "Payment amount is less than invoice total");
        }

        PaymentTransaction tx = PaymentTransaction.builder()
                .transactionRef("TXN-" + UUID.randomUUID().toString().substring(0, 10).toUpperCase())
                .invoiceId(invoice.getId())
                .amount(request.getAmount())
                .paymentMethod(request.getPaymentMethod().toUpperCase())
                .status("SETTLED")
                .build();
        PaymentTransaction savedTx = paymentRepository.save(tx);

        invoice.setStatus("PAID");
        invoice.setPaidAt(LocalDateTime.now());
        invoiceRepository.save(invoice);

        Order order = orderRepository.findById(invoice.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Order", invoice.getOrderId()));
        for (OrderItem item : order.getItems()) {
            inventoryService.commitReservation(item.getProductId(), item.getQuantity());
        }
        order.setStatus("SHIPPED");
        orderRepository.save(order);

        auditService.record("PAYMENT", savedTx.getTransactionRef(), "SETTLED", request.getActorEmail(),
                "Invoice=" + invoice.getInvoiceNumber());
        notificationService.notifyPaymentSettled(request.getActorEmail(), invoice.getInvoiceNumber(),
                savedTx.getTransactionRef());
        return savedTx;
    }

    public List<PaymentTransaction> getPaymentsForInvoice(Long invoiceId) {
        return paymentRepository.findByInvoiceId(invoiceId);
    }
}
