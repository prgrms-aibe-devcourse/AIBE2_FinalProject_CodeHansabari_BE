package com.cvmento.domain.resume.client;

import feign.RequestInterceptor;
import feign.codec.ErrorDecoder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 이력서 LLM Feign 설정.
 */
@Configuration
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
}