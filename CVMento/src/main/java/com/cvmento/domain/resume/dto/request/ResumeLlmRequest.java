package com.cvmento.domain.resume.dto.request;

/**
 * 이력서 LLM 요청.
 *
 * @param model 사용할 LLM 모델명
 * @param input LLM 입력 프롬프트
 */
public record ResumeLlmRequest(
        String model,
        String input
) {
    public static ResumeLlmRequest create(String model, String prompt) {
        return new ResumeLlmRequest(model, prompt);
    }
}