package com.my.exception;

public class EntityHasReferencesException extends RuntimeException {
    public EntityHasReferencesException(String entityName, Long entityId) {
        super("Невозможно удалить " + entityName + " с ID=" + entityId + ". Существуют связанные товары.");
    }
}
