package com.cvmento.global.exception;

/**
 * 크롤링 관련 커스텀 예외
 */
public class CrawlCoverLetterException extends RuntimeException {
    
    private final String errorCode;
    private final int httpStatus;
    
    public CrawlCoverLetterException(String errorCode, String message, int httpStatus) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
    }
    
    public CrawlCoverLetterException(String errorCode, String message, int httpStatus, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
    
    public int getHttpStatus() {
        return httpStatus;
    }
}
