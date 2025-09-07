package com.cvmento.domain.interview.dto.internal;

import com.cvmento.domain.interview.entity.CoverLetterQna;

public record ExistingQuestionDto(
        String question
) {
    public static ExistingQuestionDto from(CoverLetterQna qna) {
        return new ExistingQuestionDto(qna.getQuestion());
    }
}