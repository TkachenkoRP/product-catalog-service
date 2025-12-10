package com.my.exception;

/**
 * Исключение, выбрасываемое при попытке доступа к ресурсу без необходимых прав.
 */
public class AccessDeniedException extends RuntimeException {
    public AccessDeniedException(String message) {
        super(message);
    }
}
