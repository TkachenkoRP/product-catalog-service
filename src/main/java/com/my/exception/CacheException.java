package com.my.exception;

/**
 * Исключение, выбрасываемое при ошибках работы с кэшем.
 */
public class CacheException extends RuntimeException {
    public CacheException(String message, Throwable cause) {
        super(message, cause);
    }
}
