package com.cvmento.global.exception.customException;

public class LambdaException extends RuntimeException {
    public LambdaException(String message) {
        super(message);
    }

    public LambdaException(String message, Throwable cause) {
        super(message, cause);
    }
}