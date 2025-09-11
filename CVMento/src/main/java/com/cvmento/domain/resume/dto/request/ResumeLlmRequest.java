package com.cvmento.domain.resume.dto.request;

public record ResumeLlmRequest(
        String model,
        String input
) {
    public static ResumeLlmRequest create(String model, String prompt) {
        return new ResumeLlmRequest(model, prompt);
    }
}