package com.cvmento.domain.interview.client;

import feign.Logger;
import feign.Request;
import feign.RequestInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

import java.util.concurrent.TimeUnit;

/**
 * 인터뷰 LLM Feign 설정.
 */
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

    /**
     * 타임아웃 설정
     * - LLM API는 응답이 느릴 수 있으므로 충분한 시간 설정
     * - connectTimeout: 연결 타임아웃 (10초)
     * - readTimeout: 응답 대기 타임아웃 (5분)
     */
    @Bean
    public Request.Options options() {
        return new Request.Options(
                10, TimeUnit.SECONDS,   // connectTimeout: 10초
                5, TimeUnit.MINUTES,    // readTimeout: 5분 (300초)
                true                     // followRedirects
        );
    }
}