package com.cvmento.global.exception.customException;

public class InvalidStatusException extends RuntimeException {
    public InvalidStatusException(String message) {
        super(message);
    }
    public InvalidStatusException(String message, Throwable cause) {
        super(message, cause);
    }
}