package com.cvmento.domain.resume.exception;

/**
 * 이력서 파일 처리 관련 예외
 * - 파일 업로드, 변환, 검증 오류
 */
public class ResumeFileException extends ResumeBusinessException {
    
    public ResumeFileException(String message) {
        super(message);
    }
    
    public ResumeFileException(String message, Throwable cause) {
        super(message, cause);
    }
}