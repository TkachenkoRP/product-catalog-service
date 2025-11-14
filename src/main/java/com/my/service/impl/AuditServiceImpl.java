package com.my.service.impl;

import com.my.model.AuditLog;
import com.my.security.UserManager;
import com.my.service.AuditService;

import java.util.ArrayList;
import java.util.List;

public class AuditServiceImpl implements AuditService {
    private final List<AuditLog> logs = new ArrayList<>();

    @Override
    public void logAction(AuditLog.AuditActions action, String details) {
        AuditLog log = new AuditLog(UserManager.getLoggedInUser().getId(), action, details);
        logs.add(log);
        System.out.println("*** LOG: " + log);
    }

    @Override
    public void showAuditLog() {
        System.out.println("\nАУДИТ ЛОГ");
        if (logs.isEmpty()) {
            System.out.println("Лог пуст");
        } else {
            logs.forEach(System.out::println);
        }
    }
}
