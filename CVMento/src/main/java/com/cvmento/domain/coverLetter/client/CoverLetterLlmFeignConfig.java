package com.cvmento.domain.coverLetter.client;

import feign.Logger;
import feign.RequestInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

public class CoverLetterLlmFeignConfig {

    @Value("${llm.api.cover-letter.key}")
    private String coverLetterApiKey;

    @Bean
    public RequestInterceptor requestInterceptor() {
        return template -> {
            template.header("Authorization", "Bearer " + coverLetterApiKey);
            template.header("Content-Type", "application/json");
        };
    }

    @Bean(name = "coverLetterFeignLoggerLevel")
    public Logger.Level coverLetterFeignLoggerLevel() {
        return Logger.Level.FULL;
    }
}