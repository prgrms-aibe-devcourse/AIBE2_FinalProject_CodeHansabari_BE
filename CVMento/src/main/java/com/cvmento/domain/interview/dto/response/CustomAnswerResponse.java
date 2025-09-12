package com.cvmento.domain.interview.dto.response;

/**
 * 커스텀 질문에 대한 답변 응답.
 *
 * @param answer 답변 본문
 * @param tip    추가 팁/코칭
 */
public record CustomAnswerResponse(
        String answer,
        String tip
) { }
