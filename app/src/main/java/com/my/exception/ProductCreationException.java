package com.my.exception;

/**
 * Исключение, выбрасываемое при ошибках создания товара.
 */
public class ProductCreationException extends RuntimeException {
    public ProductCreationException(String message) {
        super(message);
    }

    public ProductCreationException(String message, Throwable cause) {
        super(message, cause);
    }
}
