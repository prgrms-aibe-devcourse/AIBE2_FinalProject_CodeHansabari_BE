package com.cvmento.domain.coverLetter.dto.response;

/**
 * LLM 분석 결과 응답 DTO
 *
 * @param feedback        AI 피드백
 * @param improvedContent 개선된 본문
 */
public record LlmAnalysisResponse(
        String feedback,
        String improvedContent
) {}
