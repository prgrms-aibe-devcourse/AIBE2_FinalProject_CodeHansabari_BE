package com.cvmento.domain.coverLetter.client;

import feign.Logger;
import feign.Request;
import feign.RequestInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

import java.util.concurrent.TimeUnit;

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