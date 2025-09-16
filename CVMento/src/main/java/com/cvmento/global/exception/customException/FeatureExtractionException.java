package com.cvmento.global.exception.customException;

/**
 * 특징 추출 관련 커스텀 예외
 */
public class FeatureExtractionException extends RuntimeException {
    public FeatureExtractionException(String message) {
        super(message);
    }

    public FeatureExtractionException(String message, Throwable cause) {
        super(message, cause);
    }
}
