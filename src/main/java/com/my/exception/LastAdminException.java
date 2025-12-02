package com.my.exception;

public class LastAdminException extends RuntimeException {
    public LastAdminException(String message) {
        super(message);
    }

    public LastAdminException() {
        super("В системе должен остаться хотя бы один администратор");
    }
}
