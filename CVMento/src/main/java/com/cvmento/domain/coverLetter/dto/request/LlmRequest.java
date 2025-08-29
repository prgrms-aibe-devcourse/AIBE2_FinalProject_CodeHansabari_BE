package com.cvmento.domain.coverLetter.dto.request;

public record LlmRequest(
        String model,
        String input
) {}