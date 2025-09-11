package com.cvmento.domain.resume.dto.response;

import java.util.List;

public record ProjectResponse(
        Long careerId,
        String startDate,
        String endDate,
        String name,
        String description,
        String detailedDescription,
        String repositoryUrl,
        String deployUrl,
        String projectType,
        List<ProjectTechStackResponse> techStacks
) {
}