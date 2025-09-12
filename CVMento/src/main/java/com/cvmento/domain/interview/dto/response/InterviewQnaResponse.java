package com.cvmento.domain.interview.dto.response;

import com.cvmento.domain.interview.entity.CoverLetterQna;

import java.time.LocalDateTime;

/**
 * 저장된 인터뷰 QnA 응답.
 *
 * @param qnaId     QnA ID
 * @param question  질문
 * @param answer    답변
 * @param tip       팁/코칭
 * @param createdAt 생성 시각
 */
public record InterviewQnaResponse(
        Long qnaId,
        String question,
        String answer,
        String tip,
        LocalDateTime createdAt
) {
    /**
     * 엔티티 → DTO 변환.
     */
    public static InterviewQnaResponse from(CoverLetterQna qna) {
        return new InterviewQnaResponse(
                qna.getCoverLetterQnaId(),
                qna.getQuestion(),
                qna.getAnswer(),
                qna.getTip(),
                qna.getCreatedAt()
        );
    }
}
