package com.cvmento.global.exception.customException;

public class AiInvalidRequestException extends RuntimeException {
    public AiInvalidRequestException(String message) {
        super(message);
    }

    public AiInvalidRequestException(String message, Throwable cause) {
        super(message, cause);
    }
}
