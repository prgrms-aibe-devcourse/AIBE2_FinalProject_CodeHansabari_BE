package com.cvmento.global.config;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MetricsConfig {

    @Bean
    public Counter loginCounter(MeterRegistry meterRegistry) {
        return Counter.builder("cvmento_user_login_total")
                .description("Total number of user logins")
                .tag("service", "auth")
                .register(meterRegistry);
    }

    @Bean
    public Counter resumeCreatedCounter(MeterRegistry meterRegistry) {
        return Counter.builder("cvmento_resume_created_total")
                .description("Total number of resumes created")
                .tag("service", "resume")
                .register(meterRegistry);
    }

    @Bean
    public Counter coverLetterCreatedCounter(MeterRegistry meterRegistry) {
        return Counter.builder("cvmento_cover_letter_created_total")
                .description("Total number of cover letters created")
                .tag("service", "coverletter")
                .register(meterRegistry);
    }

    @Bean
    public Counter interviewCreatedCounter(MeterRegistry meterRegistry) {
        return Counter.builder("cvmento_interview_created_total")
                .description("Total number of interview sessions created")
                .tag("service", "interview")
                .register(meterRegistry);
    }

    @Bean
    public Timer llmApiCallTimer(MeterRegistry meterRegistry) {
        return Timer.builder("cvmento_llm_api_call_duration")
                .description("LLM API call duration")
                .tag("service", "llm")
                .register(meterRegistry);
    }

    @Bean
    public Counter errorCounter(MeterRegistry meterRegistry) {
        return Counter.builder("cvmento_error_total")
                .description("Total number of errors")
                .register(meterRegistry);
    }
}