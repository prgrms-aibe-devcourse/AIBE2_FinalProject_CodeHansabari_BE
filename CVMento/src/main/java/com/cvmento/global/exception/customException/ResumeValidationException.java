package com.cvmento.global.exception.customException;

/**
 * 이력서 검증 실패 예외.
 */
public class ResumeValidationException extends RuntimeException {

    public ResumeValidationException(String message) {
        super(message);
    }

    public ResumeValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}