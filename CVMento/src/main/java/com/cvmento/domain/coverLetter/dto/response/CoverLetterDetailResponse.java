package com.cvmento.domain.coverLetter.dto.response;

import com.cvmento.domain.coverLetter.entity.CoverLetter;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record CoverLetterDetailResponse(
        Long coverLetterId,
        String title,
        String content,
        String jobField,  // 지원분야
        String experience, // 경력 (예: "3년", "신입")
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static CoverLetterDetailResponse from(CoverLetter coverLetter) {
        return CoverLetterDetailResponse.builder()
                .coverLetterId(coverLetter.getCoverLetterId())
                .title(coverLetter.getTitle())
                .content(coverLetter.getContent())
                .jobField(coverLetter.getJobField())
                .experience(coverLetter.getTotalExperienceString())
                .createdAt(coverLetter.getCreatedAt())
                .updatedAt(coverLetter.getUpdatedAt())
                .build();
    }
}