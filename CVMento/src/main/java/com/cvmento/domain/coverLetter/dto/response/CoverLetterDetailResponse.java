package com.cvmento.domain.coverLetter.dto.response;

import com.cvmento.domain.coverLetter.entity.CoverLetter;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record CoverLetterDetailResponse(
        Long coverLetterId,
        String title,
        String content,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static CoverLetterDetailResponse from(CoverLetter coverLetter) {
        return CoverLetterDetailResponse.builder()
                .coverLetterId(coverLetter.getCoverLetterId())
                .title(coverLetter.getTitle())
                .content(coverLetter.getContent())
                .createdAt(coverLetter.getCreatedAt())
                .updatedAt(coverLetter.getUpdatedAt())
                .build();
    }
}