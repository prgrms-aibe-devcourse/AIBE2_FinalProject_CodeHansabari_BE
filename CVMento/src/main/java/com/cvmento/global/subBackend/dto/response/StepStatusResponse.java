package com.cvmento.global.subBackend.dto.response;

import java.time.LocalDateTime;

public record StepStatusResponse(
        String step,
        String status,
        LocalDateTime startedAt,
        LocalDateTime completedAt,
        String createdBy
) {}