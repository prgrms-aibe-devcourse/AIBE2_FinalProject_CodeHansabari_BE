package com.cvmento.domain.interview.client;

import feign.Logger;
import feign.RequestInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 인터뷰 LLM Feign 설정.
 */
@Configuration
public class InterviewLlmFeignConfig {

    @Value("${llm.api.interview.key}")
    private String interviewApiKey;

    /**
     * 공통 헤더 설정(인증/JSON).
     */
    @Bean
    public RequestInterceptor requestInterceptor() {
        return template -> {
            template.header("Authorization", "Bearer " + interviewApiKey);
            template.header("Content-Type", "application/json");
        };
    }

    /**
     * 로거 레벨(BASIC).
     */
    @Bean(name = "interviewFeignLoggerLevel")
    public Logger.Level interviewFeignLoggerLevel() {
        return Logger.Level.BASIC;
    }
}
