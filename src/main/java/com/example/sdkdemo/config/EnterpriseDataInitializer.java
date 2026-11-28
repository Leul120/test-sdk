package com.example.sdkdemo.config;

import com.example.sdkdemo.model.*;
import com.example.sdkdemo.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;

@Component
@Order(2)
@RequiredArgsConstructor
@Slf4j
public class EnterpriseDataInitializer implements CommandLineRunner {

    private final DepartmentRepository departmentRepository;
    private final ProjectRepository projectRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final InvoiceRepository invoiceRepository;
    private final AuditLogRepository auditLogRepository;

    @Override
    public void run(String... args) {
        if (departmentRepository.count() == 0) {
            Department eng = departmentRepository.save(Department.builder()
                    .name("Engineering")
                    .costCenterCode("CC-ENG-100")
                    .annualBudget(new BigDecimal("500000.00"))
                    .spentBudget(new BigDecimal("125000.00"))
                    .headcountCap(40)
                    .build());

            Department mkt = departmentRepository.save(Department.builder()
                    .name("Marketing")
                    .costCenterCode("CC-MKT-200")
                    .annualBudget(new BigDecimal("250000.00"))
                    .spentBudget(new BigDecimal("60000.00"))
                    .headcountCap(15)
                    .build());

            departmentRepository.save(Department.builder()
                    .name("IT")
                    .costCenterCode("CC-IT-300")
                    .annualBudget(new BigDecimal("180000.00"))
                    .spentBudget(new BigDecimal("42000.00"))
                    .headcountCap(12)
                    .build());

            projectRepository.save(Project.builder()
                    .code("PRJ-SYN-01")
                    .name("Observability Core v3")
                    .departmentId(eng.getId())
                    .status("ACTIVE")
                    .budgetAllocated(new BigDecimal("150000.00"))
                    .budgetSpent(new BigDecimal("48000.00"))
                    .targetDate(LocalDate.now().plusMonths(3))
                    .build());

            projectRepository.save(Project.builder()
                    .code("PRJ-GTM-02")
                    .name("Enterprise Launch")
                    .departmentId(mkt.getId())
                    .status("ACTIVE")
                    .budgetAllocated(new BigDecimal("80000.00"))
                    .budgetSpent(new BigDecimal("22000.00"))
                    .targetDate(LocalDate.now().plusMonths(2))
                    .build());
        }

        if (productRepository.count() == 0) {
            productRepository.save(Product.builder()
                    .sku("HW-MBP-16")
                    .name("Workstation Laptop 16")
                    .category("HARDWARE")
                    .unitPrice(new BigDecimal("2499.00"))
                    .stockQuantity(35)
                    .reservedQuantity(2)
                    .warehouseCode("WH-US-EAST")
                    .build());

            productRepository.save(Product.builder()
                    .sku("SW-ENT-LIC")
                    .name("Enterprise Telemetry License")
                    .category("SOFTWARE")
                    .unitPrice(new BigDecimal("499.00"))
                    .stockQuantity(500)
                    .reservedQuantity(10)
                    .warehouseCode("DIGITAL")
                    .build());

            productRepository.save(Product.builder()
                    .sku("NET-GW-PRO")
                    .name("Edge Gateway Appliance")
                    .category("NETWORKING")
                    .unitPrice(new BigDecimal("899.50"))
                    .stockQuantity(8)
                    .reservedQuantity(1)
                    .warehouseCode("WH-EU-CENTRAL")
                    .build());
        }

        if (orderRepository.count() == 0) {
            com.example.sdkdemo.model.Order seededOrder = orderRepository.save(com.example.sdkdemo.model.Order.builder()
                    .orderNumber("ORD-SEED0001")
                    .userId(1L)
                    .departmentId(1L)
                    .status("CONFIRMED")
                    .subtotal(new BigDecimal("2998.00"))
                    .discountPercent(BigDecimal.ZERO)
                    .totalAmount(new BigDecimal("2998.00"))
                    .shippingRegion("US-EAST")
                    .build());

            invoiceRepository.save(Invoice.builder()
                    .invoiceNumber("INV-SEED0001")
                    .orderId(seededOrder.getId())
                    .departmentId(1L)
                    .subtotal(new BigDecimal("2998.00"))
                    .taxAmount(new BigDecimal("217.36"))
                    .totalAmount(new BigDecimal("3215.36"))
                    .status("ISSUED")
                    .dueDate(LocalDate.now().plusDays(30))
                    .build());

            auditLogRepository.save(AuditLog.builder()
                    .entityType("SYSTEM")
                    .entityId("BOOT")
                    .action("SEED_ENTERPRISE_DATA")
                    .actorEmail("system@company.com")
                    .details("Seeded departments, projects, products, and initial order/invoice")
                    .build());
        }

        log.info("Enterprise seed complete: departments={}, projects={}, products={}, orders={}",
                departmentRepository.count(), projectRepository.count(),
                productRepository.count(), orderRepository.count());
    }
}
