package com.my.exception;

/**
 * Исключение, выбрасываемое когда сущность не найдена в системе.
 */
public class EntityNotFoundException extends RuntimeException {
    public EntityNotFoundException(String message) {
        super(message);
    }
}
