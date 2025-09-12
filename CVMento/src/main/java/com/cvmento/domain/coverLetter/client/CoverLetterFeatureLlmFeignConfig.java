package com.cvmento.domain.coverLetter.client;

import feign.Logger;
import feign.RequestInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

public class CoverLetterFeatureLlmFeignConfig {

    @Value("${llm.api.feature-extraction.key}")
    private String featureExtractionApiKey;

    @Bean(name = "coverLetterFeatureRequestInterceptor")
    public RequestInterceptor coverLetterFeatureRequestInterceptor() {
        return template -> {
            template.header("Content-Type", "application/json");
            template.header("x-goog-api-key", featureExtractionApiKey);
        };
    }

    @Bean(name = "coverLetterFeatureFeignLoggerLevel")
    Logger.Level feignLoggerLevel() {
        return Logger.Level.BASIC;
    }
}
