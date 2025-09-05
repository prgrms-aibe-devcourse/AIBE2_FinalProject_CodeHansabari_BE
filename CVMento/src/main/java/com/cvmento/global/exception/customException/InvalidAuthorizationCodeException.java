package com.cvmento.global.exception.customException;

public class InvalidAuthorizationCodeException extends RuntimeException {
    public InvalidAuthorizationCodeException(String message) {
        super(message);
    }

    public InvalidAuthorizationCodeException(String message, Throwable cause) {
        super(message, cause);
    }
}