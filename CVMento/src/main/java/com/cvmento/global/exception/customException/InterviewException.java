package com.cvmento.global.exception.customException;

public class InterviewException extends RuntimeException {
    public InterviewException(String message) {
        super(message);
    }

    public InterviewException(String message, Throwable cause) {
        super(message, cause);
    }
}