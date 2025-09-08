package com.cvmento.global.exception.customException;

public class ResumeException extends RuntimeException {
    public ResumeException(String message) {
        super(message);
    }

    public ResumeException(String message, Throwable cause) {
        super(message, cause);
    }
}