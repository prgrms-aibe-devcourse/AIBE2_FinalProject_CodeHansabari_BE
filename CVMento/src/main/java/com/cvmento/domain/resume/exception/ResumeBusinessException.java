package com.cvmento.domain.resume.exception;

/**
 * 이력서 도메인 비즈니스 예외 기본 클래스
 */
public class ResumeBusinessException extends RuntimeException {
    
    public ResumeBusinessException(String message) {
        super(message);
    }
    
    public ResumeBusinessException(String message, Throwable cause) {
        super(message, cause);
    }
}