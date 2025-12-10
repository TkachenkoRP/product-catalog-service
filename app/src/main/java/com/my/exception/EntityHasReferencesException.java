package com.my.exception;

/**
 * Исключение, выбрасываемое при попытке удалить сущность, на которую есть ссылки в других сущностях.
 */
public class EntityHasReferencesException extends RuntimeException {
    public EntityHasReferencesException(String entityName, Long entityId) {
        super("Невозможно удалить " + entityName + " с ID=" + entityId + ". Существуют связанные товары.");
    }
}
