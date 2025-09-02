package com.cvmento.domain.interview.client;

import feign.Logger;
import feign.RequestInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class InterviewLlmFeignConfig {

    @Value("${llm.api.interview.key}")
    private String interviewApiKey;

    @Bean(name = "interviewRequestInterceptor")
    public RequestInterceptor interviewRequestInterceptor() {
        return template -> {
            template.header("Content-Type", "application/json");
            template.header("Authorization", "Bearer " + interviewApiKey);
        };
    }

}