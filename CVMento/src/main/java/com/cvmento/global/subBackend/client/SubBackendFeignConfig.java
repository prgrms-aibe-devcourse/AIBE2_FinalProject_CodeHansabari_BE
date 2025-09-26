package com.cvmento.global.subBackend.client;

import feign.RequestInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class SubBackendFeignConfig {

    @Value("${sub-backend.api-key}")
    private String apiKey;

    @Bean
    public RequestInterceptor requestInterceptor() {
        return requestTemplate -> {
            // 1. 서비스 간 인증 헤더 (필수)
            requestTemplate.header("X-API-Key", apiKey);
            requestTemplate.header("X-Service-Name", "main-backend");
            requestTemplate.header("Content-Type", "application/json");

            // 2. 사용자 정보 헤더 (선택적)
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getPrincipal())) {
                String userId = authentication.getName();
                requestTemplate.header("X-User-Id", userId);
            }
        };
    }
}