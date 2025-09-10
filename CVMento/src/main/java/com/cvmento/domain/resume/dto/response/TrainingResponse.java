package com.cvmento.domain.resume.dto.response;

import java.util.List;

public record TrainingResponse(
        String startDate,
        String endDate,
        String courseName,
        String institutionName,
        String detailedContent,
        List<TrainingTechStackResponse> techStacks
) {
}
