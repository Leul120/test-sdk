package com.example.sdkdemo.service;

import com.example.sdkdemo.model.Department;
import com.example.sdkdemo.model.Invoice;
import com.example.sdkdemo.model.Order;
import com.example.sdkdemo.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ReportingService {

    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final OrderRepository orderRepository;
    private final InvoiceRepository invoiceRepository;
    private final ProductRepository productRepository;
    private final TaxCalculatorService taxCalculatorService;

    public Map<String, Object> getExecutiveSummary() {
        List<Order> orders = orderRepository.findAll();
        List<Invoice> invoices = invoiceRepository.findAll();
        List<Department> departments = departmentRepository.findAll();

        BigDecimal totalRevenue = invoices.stream()
                .map(Invoice::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalTax = invoices.stream()
                .map(Invoice::getTaxAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalSubtotal = invoices.stream()
                .map(Invoice::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal effectiveTaxRate = taxCalculatorService.calculateEffectiveTaxRatePercent(totalTax, totalSubtotal);

        Map<String, Object> summary = new HashMap<>();
        summary.put("totalUsers", userRepository.count());
        summary.put("totalDepartments", departments.size());
        summary.put("totalOrders", orders.size());
        summary.put("totalInvoices", invoices.size());
        summary.put("lowStockProducts", productRepository.findLowStockProducts(10).size());
        summary.put("totalRevenue", totalRevenue);
        summary.put("effectiveTaxRatePercent", effectiveTaxRate);
        return summary;
    }
}
