package com.cvmento.domain.resume.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * LLM API 응답 DTO
 */
@Schema(description = "LLM API 응답 DTO")
public record LlmResponse(
        
        @JsonProperty("id")
        @Schema(description = "응답 ID")
        String id,
        
        @JsonProperty("object")
        @Schema(description = "객체 타입")
        String object,
        
        @JsonProperty("created")
        @Schema(description = "생성 시각")
        Long created,
        
        @JsonProperty("model")
        @Schema(description = "사용된 모델")
        String model,
        
        @JsonProperty("choices")
        @Schema(description = "응답 선택지")
        List<Choice> choices,
        
        @JsonProperty("usage")
        @Schema(description = "토큰 사용량")
        Usage usage
        
) {
    
    @Schema(description = "응답 선택지")
    public record Choice(
            @JsonProperty("index") Integer index,
            @JsonProperty("message") Message message,
            @JsonProperty("finish_reason") String finishReason
    ) {}
    
    @Schema(description = "메시지")
    public record Message(
            @JsonProperty("role") String role,
            @JsonProperty("content") String content
    ) {}
    
    @Schema(description = "토큰 사용량")
    public record Usage(
            @JsonProperty("prompt_tokens") Integer promptTokens,
            @JsonProperty("completion_tokens") Integer completionTokens,
            @JsonProperty("total_tokens") Integer totalTokens
    ) {}
    
    /**
     * 첫 번째 응답 내용 추출
     */
    public String getFirstContent() {
        if (choices == null || choices.isEmpty()) {
            return "";
        }
        
        Choice firstChoice = choices.get(0);
        if (firstChoice.message() == null) {
            return "";
        }
        
        return firstChoice.message().content() != null ? firstChoice.message().content() : "";
    }
}