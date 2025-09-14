package com.cvmento.domain.interview.dto.response;

import java.util.List;

/**
 * LLM이 생성한 인터뷰 QnA 응답(원본 형태).
 *
 * @param qnaList 질문/답변/팁 목록
 */
public record InterviewLlmResponse(
        List<InterviewQnaDto> qnaList
) { }
