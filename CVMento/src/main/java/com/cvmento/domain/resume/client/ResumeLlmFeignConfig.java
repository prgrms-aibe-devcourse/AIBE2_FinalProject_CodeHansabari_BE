package com.cvmento.domain.resume.client;

import feign.Request;
import feign.RequestInterceptor;
import feign.codec.ErrorDecoder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

import java.util.concurrent.TimeUnit;

/**
 * 이력서 LLM Feign 설정.
 */
public class ResumeLlmFeignConfig {

    @Value("${llm.api.resume.key}")
    private String apiKey;

    /**
     * 공통 헤더 설정(인증/JSON).
     */
    @Bean(name = "resumeRequestInterceptor")
    public RequestInterceptor requestInterceptor() {
        return requestTemplate -> {
            requestTemplate.header("Authorization", "Bearer " + apiKey);
            requestTemplate.header("Content-Type", "application/json");
        };
    }

    /**
     * 에러 디코더 설정.
     */
    @Bean(name = "resumeErrorDecoder")
    public ErrorDecoder errorDecoder() {
        return new ErrorDecoder.Default();
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