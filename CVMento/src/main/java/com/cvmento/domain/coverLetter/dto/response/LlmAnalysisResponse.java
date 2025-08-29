package com.cvmento.domain.coverLetter.dto.response;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public record LlmAnalysisResponse(
        String feedback,
        String improvedContent
) {
}
