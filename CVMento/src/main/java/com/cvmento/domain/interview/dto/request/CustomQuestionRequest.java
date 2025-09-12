package com.cvmento.domain.interview.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 커스텀 질문 생성 요청.
 *
 * @param question 질문(최대 500자)
 */
public record CustomQuestionRequest(
        @NotBlank(message = "질문은 필수입니다.")
        @Size(max = 500, message = "질문은 최대 500자까지 가능합니다.")
        String question
) { }
