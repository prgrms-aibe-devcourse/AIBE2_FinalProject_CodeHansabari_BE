package com.cvmento.global.exception.customException;

public class ResumeAiException extends RuntimeException {
    public ResumeAiException(String message) {
        super(message);
    }

    public ResumeAiException(String message, Throwable cause) {
        super(message, cause);
    }
}