package com.cvmento.domain.coverLetter.dto.response;

/**
 * 크롤링 결과 응답 DTO
 */
public record CrawlCoverLetterResponse(
    boolean success,
    String message,
    int crawledCount
) {
    
    /**
     * 성공 응답 생성
     */
    public static CrawlCoverLetterResponse success(String message, int crawledCount) {
        return new CrawlCoverLetterResponse(true, message, crawledCount);
    }
    
    /**
     * 실패 응답 생성
     */
    public static CrawlCoverLetterResponse failure(String message) {
        return new CrawlCoverLetterResponse(false, message, 0);
    }
}
