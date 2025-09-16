package com.cvmento.global.exception.customException;

/**
 * 이력서 변환 실패 예외.
 */
public class ResumeConversionException extends RuntimeException {

    public ResumeConversionException(String message) {
        super(message);
    }

    public ResumeConversionException(String message, Throwable cause) {
        super(message, cause);
    }
}