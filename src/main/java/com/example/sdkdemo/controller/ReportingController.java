package com.example.sdkdemo.controller;

import com.example.sdkdemo.dto.ApiResponse;
import com.example.sdkdemo.model.AuditLog;
import com.example.sdkdemo.service.AuditService;
import com.example.sdkdemo.service.NotificationService;
import com.example.sdkdemo.service.ReportingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportingController {

    private final ReportingService reportingService;
    private final AuditService auditService;
    private final NotificationService notificationService;

    @GetMapping("/executive-summary")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getExecutiveSummary() {
        return ResponseEntity.ok(ApiResponse.success(reportingService.getExecutiveSummary()));
    }

    @GetMapping("/audit-logs")
    public ResponseEntity<ApiResponse<List<AuditLog>>> getAuditLogs() {
        return ResponseEntity.ok(ApiResponse.success(auditService.getAllLogs()));
    }

    @GetMapping("/notifications")
    public ResponseEntity<ApiResponse<List<String>>> getNotifications() {
        return ResponseEntity.ok(ApiResponse.success(notificationService.getRecentNotifications()));
    }
}
