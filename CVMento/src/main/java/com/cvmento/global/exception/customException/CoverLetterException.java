package com.cvmento.global.exception.customException;

public class CoverLetterException extends RuntimeException {
    public CoverLetterException(String message) {
        super(message);
    }

    public CoverLetterException(String message, Throwable cause) {
        super(message, cause);
    }
}