package com.example.sdkdemo.controller;

import com.example.sdkdemo.dto.ApiResponse;
import com.example.sdkdemo.dto.PaymentRequest;
import com.example.sdkdemo.model.Invoice;
import com.example.sdkdemo.model.PaymentTransaction;
import com.example.sdkdemo.service.BillingService;
import com.example.sdkdemo.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/billing")
@RequiredArgsConstructor
public class BillingController {

    private final BillingService billingService;
    private final PaymentService paymentService;

    @GetMapping("/invoices")
    public ResponseEntity<ApiResponse<List<Invoice>>> listInvoices() {
        return ResponseEntity.ok(ApiResponse.success(billingService.getAllInvoices()));
    }

    @GetMapping("/invoices/{invoiceNumber}")
    public ResponseEntity<ApiResponse<Invoice>> getInvoice(@PathVariable String invoiceNumber) {
        return ResponseEntity.ok(ApiResponse.success(billingService.getByInvoiceNumber(invoiceNumber)));
    }

    @PostMapping("/payments")
    public ResponseEntity<ApiResponse<PaymentTransaction>> payInvoice(@RequestBody PaymentRequest request) {
        PaymentTransaction tx = paymentService.processPayment(request);
        return ResponseEntity.ok(ApiResponse.success(tx, "Payment settled"));
    }

    @GetMapping("/invoices/{invoiceId}/payments")
    public ResponseEntity<ApiResponse<List<PaymentTransaction>>> getPayments(@PathVariable Long invoiceId) {
        return ResponseEntity.ok(ApiResponse.success(paymentService.getPaymentsForInvoice(invoiceId)));
    }
}
