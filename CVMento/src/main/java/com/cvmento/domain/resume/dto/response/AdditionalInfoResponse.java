package com.cvmento.domain.resume.dto.response;

public record AdditionalInfoResponse(
        String startDate,
        String endDate,
        String category,
        String activityName,
        String relatedOrganization,
        String detailedContent,
        String certificateNumber,
        String languageLevel
) {
}
