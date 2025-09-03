package com.cvmento.domain.interview.dto.response;

public record InterviewQnaDto(
        String question,
        String answer,
        String tip
) {}