package com.cvmento.domain.coverLetter.dto.response;

import com.cvmento.domain.coverLetter.entity.CoverLetter;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record CoverLetterListResponse(
        Long coverLetterId,
        String title,
        String content,  // view 옵션에 따라 전체 내용 또는 미리보기
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    // 썸네일 뷰 (미리보기)
    public static CoverLetterListResponse thumbnail(CoverLetter coverLetter) {
        String preview = coverLetter.getContent().length() > 100
                ? coverLetter.getContent().substring(0, 100) + "..."
                : coverLetter.getContent();

        return CoverLetterListResponse.builder()
                .coverLetterId(coverLetter.getCoverLetterId())
                .title(coverLetter.getTitle())
                .content(preview)
                .createdAt(coverLetter.getCreatedAt())
                .updatedAt(coverLetter.getUpdatedAt())
                .build();
    }

    // 전체 뷰 (전체 내용)
    public static CoverLetterListResponse full(CoverLetter coverLetter) {
        return CoverLetterListResponse.builder()
                .coverLetterId(coverLetter.getCoverLetterId())
                .title(coverLetter.getTitle())
                .content(coverLetter.getContent())
                .createdAt(coverLetter.getCreatedAt())
                .updatedAt(coverLetter.getUpdatedAt())
                .build();
    }
}