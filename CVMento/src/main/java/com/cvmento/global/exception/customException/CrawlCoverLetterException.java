package com.cvmento.global.exception.customException;

/**
 * 크롤링 관련 커스텀 예외
 */
public class CrawlCoverLetterException extends RuntimeException {
    public CrawlCoverLetterException(String message) {
        super(message);
    }

    public CrawlCoverLetterException(String message, Throwable cause) {
        super(message, cause);
    }
}
