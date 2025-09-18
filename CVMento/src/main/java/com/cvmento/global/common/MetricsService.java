package com.cvmento.global.common;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class MetricsService {

    private final Counter loginCounter;
    private final Counter resumeCreatedCounter;
    private final Counter coverLetterCreatedCounter;
    private final Counter interviewCreatedCounter;
    private final Timer llmApiCallTimer;
    private final MeterRegistry meterRegistry;

    public void incrementLoginCount() {
        loginCounter.increment();
        log.debug("Login count incremented");
    }

    public void incrementResumeCreatedCount() {
        resumeCreatedCounter.increment();
        log.debug("Resume created count incremented");
    }

    public void incrementCoverLetterCreatedCount() {
        coverLetterCreatedCounter.increment();
        log.debug("Cover letter created count incremented");
    }

    public void incrementInterviewCreatedCount() {
        interviewCreatedCounter.increment();
        log.debug("Interview created count incremented");
    }

    public Timer.Sample startLlmApiCallTimer() {
        return Timer.start();
    }

    public void stopLlmApiCallTimer(Timer.Sample sample) {
        sample.stop(llmApiCallTimer);
        log.debug("LLM API call timer stopped");
    }

    public void incrementErrorCount(String errorType) {
        Counter.builder("cvmento_error_total")
                .tag("type", errorType)
                .description("Total number of errors by type")
                .register(meterRegistry)
                .increment();
        log.debug("Error count incremented for type: {}", errorType);
    }
}