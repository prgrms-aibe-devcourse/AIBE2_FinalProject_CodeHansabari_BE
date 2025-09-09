package com.cvmento.domain.resume.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * LLM API 요청 DTO (인터뷰 AI와 동일한 형식)
 */
@Schema(description = "LLM API 요청 DTO")
public record LlmRequest(
        
        @Schema(description = "모델명", example = "gpt-5-nano")
        String model,
        
        @Schema(description = "프롬프트 입력", example = "당신은 전문 이력서 컨설턴트입니다...")
        String input
        
) {
    
    /**
     * 기본 설정으로 요청 생성
     */
    public static LlmRequest createDefault(String prompt) {
        return new LlmRequest("gpt-5-nano", prompt);
    }
    
    /**
     * 이력서 제안용 요청 생성
     */
    public static LlmRequest createForResumeSuggestion(String prompt) {
        return new LlmRequest("gpt-5-nano", prompt);
    }
    
    /**
     * 섹션 개선용 요청 생성
     */
    public static LlmRequest createForSectionImprovement(String prompt) {
        return new LlmRequest("gpt-5-nano", prompt);
    }
}