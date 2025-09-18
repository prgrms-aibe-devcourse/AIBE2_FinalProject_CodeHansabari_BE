package com.cvmento.domain.coverLetter.dto.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;

public record GeminiRequest(
    List<Content> contents,
    GenerationConfig generationConfig
) {
    public record Content(
        List<Part> parts
    ) {
        public record Part(
            String text
        ) {}
    }
    
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record GenerationConfig(
        String temperature,
        String maxOutputTokens,
        String topP,
        String topK,
        String responseMimeType,
        Object responseSchema,
        ThinkingConfig thinkingConfig
    ) {
        
        @JsonInclude(JsonInclude.Include.NON_NULL)
        public record ThinkingConfig(
            String thinkingBudget
        ) {}
        // Gemini 2.5 Flash에 최적화된 기본 설정
        public GenerationConfig(String temperature, String maxOutputTokens) {
            this(temperature, maxOutputTokens, "0.95", "40", "application/json", null, null);
        }
        
        // JSON 구조화된 응답을 위한 설정
        public GenerationConfig(String temperature, String maxOutputTokens, String responseMimeType, Object responseSchema) {
            this(temperature, maxOutputTokens, "0.95", "40", responseMimeType, responseSchema, null);
        }
        
        // 사고 예산(Thinking Budget)을 포함한 고급 설정
        public GenerationConfig(String temperature, String maxOutputTokens, String responseMimeType, Object responseSchema, String thinkingBudget) {
            this(temperature, maxOutputTokens, "0.95", "40", responseMimeType, responseSchema, 
                 thinkingBudget != null ? new ThinkingConfig(thinkingBudget) : null);
        }
    }
}
