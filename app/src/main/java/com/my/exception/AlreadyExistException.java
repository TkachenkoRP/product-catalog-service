package com.my.exception;

/**
 * Исключение, выбрасываемое при попытке создать сущность, которая уже существует.
 */
public class AlreadyExistException extends RuntimeException {
    public AlreadyExistException(String message) {
        super(message);
    }
}
