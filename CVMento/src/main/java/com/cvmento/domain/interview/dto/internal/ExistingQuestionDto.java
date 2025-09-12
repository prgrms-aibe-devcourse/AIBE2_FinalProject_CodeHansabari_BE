package com.cvmento.domain.interview.dto.internal;

import com.cvmento.domain.interview.entity.CoverLetterQna;

/**
 * 기존 보유 질문 DTO.
 *
 * @param question 질문 내용
 */
public record ExistingQuestionDto(
        String question
) {
    /**
     * 엔티티 → DTO 변환.
     */
    public static ExistingQuestionDto from(CoverLetterQna qna) {
        return new ExistingQuestionDto(qna.getQuestion());
    }
}
