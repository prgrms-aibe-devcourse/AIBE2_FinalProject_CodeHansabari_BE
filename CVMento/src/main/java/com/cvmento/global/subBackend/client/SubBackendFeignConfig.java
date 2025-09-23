package com.cvmento.global.subBackend.client;

import feign.RequestInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

public class SubBackendFeignConfig {

    @Value("${sub-backend.api-key}")
    private String apiKey;

    @Bean
    public RequestInterceptor requestInterceptor() {
        return template -> {
            template.header("X-API-Key", apiKey);
            template.header("X-Service-Name", "main-backend");
            template.header("Content-Type", "application/json");
        };
    }
}