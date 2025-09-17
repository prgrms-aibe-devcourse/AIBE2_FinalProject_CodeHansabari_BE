package com.cvmento.domain.coverLetter.dto.response;

import com.cvmento.global.common.dto.CommonResponse;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * 추출된 특징 페이징 응답을 위한 DTO
 */
public record CoverLetterFeaturePageResponse(
    List<CoverLetterFeatureData> content,
    long totalElements,
    int totalPages,
    int currentPage,
    int pageSize,
    boolean hasNext,
    boolean hasPrevious
) {
    /**
     * Page<CoverLetterFeature>를 CoverLetterFeaturePageResponse로 변환
     */
    public static CoverLetterFeaturePageResponse from(Page<CoverLetterFeatureData> page) {
        return new CoverLetterFeaturePageResponse(
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