package com.cvmento.domain.resume.dto.response;

public record ResumeTechStackResponse(
        Long techStackId,
        String techStackName,
        String category,
        String proficiencyLevel
) {
}