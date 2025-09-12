package com.cvmento.domain.coverLetter.dto.response;

import com.cvmento.domain.coverLetter.entity.CoverLetter;

import java.time.LocalDateTime;

/**
 * 자소서 상세 응답 DTO
 */
public record CoverLetterDetailResponse(
        Long coverLetterId,
        String title,
        String content,
        String jobField,
        String experience,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static CoverLetterDetailResponse from(CoverLetter coverLetter) {
        return new CoverLetterDetailResponse(
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
