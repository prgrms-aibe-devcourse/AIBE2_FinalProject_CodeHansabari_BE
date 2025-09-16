package com.cvmento.domain.coverLetter.dto.response;

import java.util.List;

/**
 * 크롤링 데이터 페이징 응답 DTO
 *
 * @param content 데이터 목록
 * @param totalElements 전체 요소 개수
 * @param totalPages 전체 페이지 수
 * @param currentPage 현재 페이지 (0부터 시작)
 * @param pageSize 페이지 크기
 * @param hasNext 다음 페이지 존재 여부
 * @param hasPrevious 이전 페이지 존재 여부
 */
public record CrawlCoverLetterPageResponse(
        List<CrawlCoverLetterData> content,
        long totalElements,
        int totalPages,
        int currentPage,
        int pageSize,
        boolean hasNext,
        boolean hasPrevious
) {
    /**
     * Spring Data Page 객체로부터 DTO 생성
     */
    public static CrawlCoverLetterPageResponse from(org.springframework.data.domain.Page<CrawlCoverLetterData> page) {
        return new CrawlCoverLetterPageResponse(
                page.getContent(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.getNumber(),
                page.getSize(),
                page.hasNext(),
                page.hasPrevious()
        );
    }
}
