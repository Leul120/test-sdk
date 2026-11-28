package com.example.sdkdemo.repository;

import com.example.sdkdemo.model.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    List<AuditLog> findByEntityTypeAndEntityId(String entityType, String entityId);
    List<AuditLog> findByActorEmail(String actorEmail);
    List<AuditLog> findBySeverity(String severity);
}
