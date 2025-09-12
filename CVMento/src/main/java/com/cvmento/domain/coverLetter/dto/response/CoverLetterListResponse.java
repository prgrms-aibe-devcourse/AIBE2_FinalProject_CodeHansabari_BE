package com.cvmento.domain.coverLetter.dto.response;

import com.cvmento.domain.coverLetter.entity.CoverLetter;

import java.time.LocalDateTime;

/**
 * 자소서 리스트 응답 DTO (미리보기/전체 뷰 공용)
 */
public record CoverLetterListResponse(
        Long coverLetterId,
        String title,
        String content,
        String jobField,
        String experience,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    /** 썸네일 뷰 (미리보기 100자) */
    public static CoverLetterListResponse thumbnail(CoverLetter coverLetter) {
        String raw = coverLetter.getContent();
        String preview = (raw != null && raw.length() > 100) ? raw.substring(0, 100) + "..." : raw;

        return new CoverLetterListResponse(
                coverLetter.getCoverLetterId(),
                coverLetter.getTitle(),
                preview,
                coverLetter.getJobField(),
                coverLetter.getTotalExperienceString(),
                coverLetter.getCreatedAt(),
                coverLetter.getUpdatedAt()
        );
    }

    /** 전체 뷰 (전체 내용) */
    public static CoverLetterListResponse full(CoverLetter coverLetter) {
        return new CoverLetterListResponse(
                coverLetter.getCoverLetterId(),
                coverLetter.getTitle(),
                coverLetter.getContent(),
                coverLetter.getJobField(),
                coverLetter.getTotalExperienceString(),
                coverLetter.getCreatedAt(),
                coverLetter.getUpdatedAt()
        );
    }
}
