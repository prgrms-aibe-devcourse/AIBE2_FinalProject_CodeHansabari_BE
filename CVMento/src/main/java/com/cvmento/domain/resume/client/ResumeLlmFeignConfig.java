package com.cvmento.domain.resume.client;

import feign.Logger;
import feign.RequestInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ResumeLlmFeignConfig {

    @Value("${llm.api.resume.key}")
    private String llmApiKey;

    @Bean
    public RequestInterceptor requestInterceptor() {
        return template -> {
            template.header("Authorization", "Bearer " + llmApiKey);
            template.header("Content-Type", "application/json");
        };
    }

    @Bean
    Logger.Level resumefeignLoggerLevel() {
        return Logger.Level.BASIC;
    }

}