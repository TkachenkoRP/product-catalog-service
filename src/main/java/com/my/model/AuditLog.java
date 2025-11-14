package com.my.model;

import java.time.LocalDateTime;

public class AuditLog {
    private final Long userId;
    private final AuditActions action;
    private final String details;
    private final LocalDateTime timestamp;

    public AuditLog(Long userId, AuditActions action, String details) {
        this.userId = userId;
        this.action = action;
        this.details = details;
        this.timestamp = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return String.format("[%s] %s: %s - %s",
                timestamp, userId, action, details);
    }

    public enum AuditActions {
        LOGIN,
        LOGOUT,
        VIEW_ALL_PRODUCTS,
        VIEW_PRODUCT,
        ADD_PRODUCT,
        UPDATE_PRODUCT,
        DELETE_PRODUCT,
        VIEW_ALL_CATEGORIES,
        ADD_CATEGORY,
        UPDATE_CATEGORY,
        DELETE_CATEGORY,
        VIEW_ALL_BRANDS,
        ADD_BRAND,
        UPDATE_BRAND,
        DELETE_BRAND,
    }
}
