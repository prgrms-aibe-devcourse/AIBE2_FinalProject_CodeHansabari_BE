package com.cvmento.global.exception.customException;

public class InvalidAnalysisStepException extends RuntimeException {
    public InvalidAnalysisStepException(String message) {
        super(message);
    }
    public InvalidAnalysisStepException(String message, Throwable cause) {
        super(message, cause);
    }
}