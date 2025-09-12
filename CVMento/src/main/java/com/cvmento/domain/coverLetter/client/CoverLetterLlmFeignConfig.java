package com.cvmento.domain.coverLetter.client;

import feign.Logger;
import feign.RequestInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

/**
 * 자소서 LLM Feign 설정
 */
public class CoverLetterLlmFeignConfig {

    @Value("${llm.api.cover-letter.key}")
    private String coverLetterApiKey;

    /**
     * 공통 헤더 설정 (Authorization, Content-Type)
     */
    @Bean
    public RequestInterceptor requestInterceptor() {
        return template -> {
            template.header("Authorization", "Bearer " + coverLetterApiKey);
            template.header("Content-Type", "application/json");
        };
    }

    /**
     * Feign 로깅 레벨 (요청/응답 전체)
     */
    @Bean(name = "coverLetterFeignLoggerLevel")
    public Logger.Level coverLetterFeignLoggerLevel() {
        return Logger.Level.FULL;
    }
}
