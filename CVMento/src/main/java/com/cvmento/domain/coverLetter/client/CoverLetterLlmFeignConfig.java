package com.cvmento.domain.coverLetter.client;

import feign.Logger;
import feign.RequestInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CoverLetterLlmFeignConfig {

    @Value("${llm.api.cover-letter.key}")
    private String coverLetterApiKey;

    @Bean
    public RequestInterceptor coverLetterRequestInterceptor() {
        return template -> {
            template.header("Content-Type", "application/json");
            template.header("Authorization", "Bearer " + coverLetterApiKey);
        };
    }

    @Bean
    Logger.Level feignLoggerLevel() {
        return Logger.Level.BASIC;
    }
}