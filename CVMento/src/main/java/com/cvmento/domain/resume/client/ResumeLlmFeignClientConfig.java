package com.cvmento.domain.resume.client;

import feign.Logger;
import feign.Request;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * 이력서 LLM Feign Client 설정
 */
@Configuration
@Slf4j
public class ResumeLlmFeignClientConfig {

    /**
     * 요청/응답 타임아웃 설정
     */
    @Bean
    public Request.Options options() {
        return new Request.Options(
                15000,  // 연결 타임아웃 15초 (밀리초)
                180000  // 읽기 타임아웃 180초 (3분) (밀리초) - LLM 응답 시간 고려하여 증가
        );
    }

    /**
     * 로그 레벨 설정
     */
    @Bean
    public Logger.Level loggerLevel() {
        return Logger.Level.BASIC; // 기본 로그만 출력
    }

    /**
     * 에러 디코더 설정
     */
    @Bean
    public ErrorDecoder errorDecoder() {
        return new ResumeLlmFeignErrorDecoder();
    }

    /**
     * LLM API 에러 처리를 위한 커스텀 에러 디코더
     */
    @Slf4j
    public static class ResumeLlmFeignErrorDecoder implements ErrorDecoder {
        
        private final ErrorDecoder defaultErrorDecoder = new Default();

        @Override
        public Exception decode(String methodKey, feign.Response response) {
            log.error("LLM API 호출 실패 - Method: {}, Status: {}, Reason: {}", 
                    methodKey, response.status(), response.reason());
            
            return switch (response.status()) {
                case 400 -> new IllegalArgumentException("잘못된 요청입니다.");
                case 401 -> new SecurityException("API 키가 유효하지 않습니다.");
                case 429 -> new RuntimeException("API 호출 한도를 초과했습니다.");
                case 500 -> new RuntimeException("LLM 서비스에 오류가 발생했습니다.");
                default -> defaultErrorDecoder.decode(methodKey, response);
            };
        }
    }
}