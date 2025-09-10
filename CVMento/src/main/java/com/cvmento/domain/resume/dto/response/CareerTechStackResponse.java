package com.cvmento.domain.resume.dto.response;

public record CareerTechStackResponse(
        Long techStackId,
        String techStackName,
        String category
) {
}
