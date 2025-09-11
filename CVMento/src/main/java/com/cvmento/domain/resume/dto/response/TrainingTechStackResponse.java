package com.cvmento.domain.resume.dto.response;

public record TrainingTechStackResponse(
        Long techStackId,
        String techStackName,
        String category
) {
}