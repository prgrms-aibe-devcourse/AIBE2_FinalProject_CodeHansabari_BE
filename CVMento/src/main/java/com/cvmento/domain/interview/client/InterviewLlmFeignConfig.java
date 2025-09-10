package com.cvmento.domain.interview.client;

import feign.Logger;
import feign.RequestInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

public class InterviewLlmFeignConfig {

    @Value("${llm.api.interview.key}")
    private String interviewApiKey;

    @Bean
    public RequestInterceptor requestInterceptor() {
        return template -> {
            template.header("Authorization", "Bearer " + interviewApiKey);
            template.header("Content-Type", "application/json");
        };
    }

    @Bean(name = "interviewFeignLoggerLevel")
    public Logger.Level interviewFeignLoggerLevel() {
        return Logger.Level.BASIC;
    }

}