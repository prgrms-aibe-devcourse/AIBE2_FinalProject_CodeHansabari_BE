package com.cvmento.domain.coverLetter.dto.request;

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
    
    public record GenerationConfig(
        String temperature,
        String maxOutputTokens,
        String topP,
        String topK
    ) {
        // Gemini 2.5 Flash에 최적화된 기본 설정
        public GenerationConfig(String temperature, String maxOutputTokens) {
            this(temperature, maxOutputTokens, "0.95", "40");
        }
    }
}
