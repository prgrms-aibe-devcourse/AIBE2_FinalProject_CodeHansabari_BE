package com.cvmento.domain.coverLetter.client;

import feign.Logger;
import feign.RequestInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

public class FeatureExtractionLlmFeignConfig {

    @Value("${llm.api.feature-extraction.key}")
    private String featureExtractionApiKey;

    @Bean(name = "featureExtractionRequestInterceptor")
    public RequestInterceptor featureExtractionRequestInterceptor() {
        return template -> {
            template.header("Content-Type", "application/json");
            template.header("Authorization", "Bearer " + featureExtractionApiKey);
        };
    }

    @Bean(name = "featureExtractionFeignLoggerLevel")
    Logger.Level feignLoggerLevel() {
        return Logger.Level.BASIC;
    }
}
