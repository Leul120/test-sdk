package com.example.sdkdemo.service;

import com.example.sdkdemo.model.AuditLog;
import com.example.sdkdemo.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    public AuditLog record(String entityType, String entityId, String action, String actorEmail, String details) {
        AuditLog entry = AuditLog.builder()
                .entityType(entityType)
                .entityId(entityId)
                .action(action)
                .actorEmail(actorEmail)
                .details(details)
                .severity("INFO")
                .build();
        log.debug("Audit [{} {}] {} by {}", entityType, entityId, action, actorEmail);
        return auditLogRepository.save(entry);
    }

    public List<AuditLog> getHistory(String entityType, String entityId) {
        return auditLogRepository.findByEntityTypeAndEntityId(entityType, entityId);
    }

    public List<AuditLog> getAllLogs() {
        return auditLogRepository.findAll();
    }
}
