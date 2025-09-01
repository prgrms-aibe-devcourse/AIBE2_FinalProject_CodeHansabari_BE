package com.cvmento.domain.coverLetter.dto.response;

import com.cvmento.domain.coverLetter.entity.CoverLetter;

import java.time.LocalDateTime;

public record CoverLetterSaveResponse(
        Long coverLetterId,
        String title,
        String content,
        LocalDateTime createdAt
) {
    public static CoverLetterSaveResponse from(CoverLetter coverLetter) {
        return new CoverLetterSaveResponse(
                coverLetter.getCoverLetterId(),
                coverLetter.getTitle(),
                coverLetter.getContent(),
                coverLetter.getCreatedAt()
        );
    }
}
