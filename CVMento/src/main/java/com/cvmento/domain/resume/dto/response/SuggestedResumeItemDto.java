package com.cvmento.domain.resume.dto.response;

public record SuggestedResumeItemDto(
        String title,
        String subTitle,
        String startDate,
        String endDate,
        String description
) {
}
