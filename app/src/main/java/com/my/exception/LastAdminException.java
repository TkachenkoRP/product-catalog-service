package com.my.exception;

/**
 * Исключение, выбрасываемое при попытке удалить последнего администратора в системе.
 * Наследуется от RuntimeException, что делает его непроверяемым исключением.
 */
public class LastAdminException extends RuntimeException {
    public LastAdminException(String message) {
        super(message);
    }

    public LastAdminException() {
        super("В системе должен остаться хотя бы один администратор");
    }
}
