package com.cvmento.domain.coverLetter.client;

import feign.Logger;
import feign.RequestInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;

public class CoverLetterFeatureLlmFeignConfig {

    @Value("${gemini.api.feature-extraction.key}")
    private String featureExtractionApiKey;

    @Bean("geminiRequestInterceptor")
    public RequestInterceptor geminiRequestInterceptor() {
        return template -> {
            // Authorization 헤더 제거 (Gemini는 x-goog-api-key 사용)
            template.header("Authorization", new String[0]);
            
            // Gemini 전용 헤더 설정
            template.header("Content-Type", "application/json");
            template.header("Accept", "application/json");
            template.header("x-goog-api-key", featureExtractionApiKey);
        };
    }

    @Bean("geminiFeignLoggerLevel")
    Logger.Level geminiFeignLoggerLevel() {
        // 디버깅을 위해 FULL로 변경 (헤더/바디 출력)
        return Logger.Level.FULL;
    }
}
