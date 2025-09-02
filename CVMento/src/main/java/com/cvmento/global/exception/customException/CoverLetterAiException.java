package com.cvmento.global.exception.customException;

public class CoverLetterAiException extends RuntimeException {
    public CoverLetterAiException(String message) {
        super(message);
    }

    public CoverLetterAiException(String message, Throwable cause) {
        super(message, cause);
    }
}