package com.bingoai.backend.audit;

import com.bingoai.backend.admin.Admin;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    public AuditLogService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Transactional
    public void logAction(Admin admin, String action, String details) {
        AuditLog log = new AuditLog();
        log.setAdmin(admin);
        log.setAction(action);
        log.setDetails(details);
        auditLogRepository.save(log);
    }
    
    @Transactional
    public void logSystemAction(String action, String details) {
        AuditLog log = new AuditLog();
        log.setAction(action);
        log.setDetails(details);
        auditLogRepository.save(log);
    }
}
