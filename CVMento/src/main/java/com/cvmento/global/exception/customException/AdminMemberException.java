package com.cvmento.global.exception.customException;

public class AdminMemberException extends RuntimeException {
    public AdminMemberException(String message) {
        super(message);
    }

    public AdminMemberException(String message, Throwable cause) {
        super(message, cause);
    }
}