package com.cvmento.domain.resume.client;

import feign.Logger;
import feign.RequestInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;



public class ResumeLlmVisionFeignConfig {

    @Value("${llm.api.resume.vision.key}") // Using a new, specific key for vision
    private String llmVisionApiKey;

    @Bean("resumeVisionRequestInterceptor") // Unique bean name to avoid collision
    public RequestInterceptor requestInterceptor() {
        return template -> {
            template.header("Authorization", "Bearer " + llmVisionApiKey);
            template.header("Content-Type", "application/json");
        };
    }

    @Bean
    Logger.Level resumefeignVisionLoggerLevel() { // Unique method name for logger level
        return Logger.Level.BASIC;
    }
}
