package com.cvmento.domain.coverLetter.dto.request;

/**
 * LLM 요청 DTO
 *
 * @param model  사용할 모델 이름
 * @param input  입력 프롬프트
 */
public record LlmRequest(
        String model,
        String input
) {}
