package com.example.sdkdemo.service;

import com.example.sdkdemo.exception.BusinessException;
import com.example.sdkdemo.exception.ResourceNotFoundException;
import com.example.sdkdemo.model.Department;
import com.example.sdkdemo.model.Invoice;
import com.example.sdkdemo.model.Order;
import com.example.sdkdemo.repository.DepartmentRepository;
import com.example.sdkdemo.repository.InvoiceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class BillingService {

    private final InvoiceRepository invoiceRepository;
    private final DepartmentRepository departmentRepository;
    private final TaxCalculatorService taxCalculatorService;
    private final AuditService auditService;

    @Transactional
    public Invoice issueInvoiceForOrder(Order order, BigDecimal discountedSubtotal) {
        BigDecimal tax = taxCalculatorService.calculateTax(discountedSubtotal, order.getShippingRegion());
        BigDecimal total = discountedSubtotal.add(tax);

        if (order.getDepartmentId() != null) {
            Department dept = departmentRepository.findById(order.getDepartmentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Department", order.getDepartmentId()));
            BigDecimal projectedSpend = dept.getSpentBudget().add(total);
            if (projectedSpend.compareTo(dept.getAnnualBudget()) > 0) {
                throw new BusinessException("BUDGET_EXCEEDED",
                        "Order exceeds annual budget for department " + dept.getName());
            }
            dept.setSpentBudget(projectedSpend);
            departmentRepository.save(dept);
        }

        Invoice invoice = Invoice.builder()
                .invoiceNumber("INV-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .orderId(order.getId())
                .departmentId(order.getDepartmentId())
                .subtotal(discountedSubtotal)
                .taxAmount(tax)
                .discountAmount(order.getSubtotal().subtract(discountedSubtotal))
                .totalAmount(total)
                .status("ISSUED")
                .dueDate(LocalDate.now().plusDays(30))
                .build();

        Invoice saved = invoiceRepository.save(invoice);
        auditService.record("INVOICE", saved.getInvoiceNumber(), "ISSUED", "billing-engine",
                "Total=" + saved.getTotalAmount());
        return saved;
    }

    public List<Invoice> getAllInvoices() {
        return invoiceRepository.findAll();
    }

    public Invoice getByInvoiceNumber(String invoiceNumber) {
        return invoiceRepository.findByInvoiceNumber(invoiceNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice", invoiceNumber));
    }
}
