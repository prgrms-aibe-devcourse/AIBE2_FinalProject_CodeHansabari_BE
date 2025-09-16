package com.cvmento.domain.coverLetter.dto.request;

import java.util.List;

/**
 * LLM 요청 DTO
 *
 * @param model 사용할 모델 이름
 * @param input 시스템/유저 입력 배열
 */
public record LlmRequest(
        String model,
        List<InputItem> input
) {}