package com.cvmento.domain.interview.dto.response;

/**
 * 인터뷰 QnA 단건.
 *
 * @param question 질문
 * @param answer   답변
 * @param tip      팁/후속 코칭
 */
public record InterviewQnaDto(
        String question,
        String answer,
        String tip
) { }
