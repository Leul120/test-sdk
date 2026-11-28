package com.example.sdkdemo.repository;

import com.example.sdkdemo.model.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    Optional<Invoice> findByInvoiceNumber(String invoiceNumber);
    Optional<Invoice> findByOrderId(Long orderId);
    List<Invoice> findByDepartmentId(Long departmentId);
    List<Invoice> findByStatus(String status);
}
