package com.cvmento.domain.resume.dto.response;

public record ProjectTechStackResponse(
        Long techStackId,
        String techStackName,
        String category,
        String usageType
) {
}
