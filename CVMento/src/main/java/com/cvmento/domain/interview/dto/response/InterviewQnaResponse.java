package com.cvmento.domain.interview.dto.response;

import com.cvmento.domain.interview.entity.CoverLetterQna;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record InterviewQnaResponse(
        Long qnaId,
        String question,
        String answer,
        String tip,
        LocalDateTime createdAt
) {
    public static InterviewQnaResponse from(CoverLetterQna qna) {
        return InterviewQnaResponse.builder()
                .qnaId(qna.getCoverLetterQnaId())
                .question(qna.getQuestion())
                .answer(qna.getAnswer())
                .tip(qna.getTip())
                .createdAt(qna.getCreatedAt())
                .build();
    }
}