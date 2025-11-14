package com.my.service;

import com.my.model.AuditLog;

public interface AuditService {
    void logAction(AuditLog.AuditActions action, String details);
    void showAuditLog();
}
