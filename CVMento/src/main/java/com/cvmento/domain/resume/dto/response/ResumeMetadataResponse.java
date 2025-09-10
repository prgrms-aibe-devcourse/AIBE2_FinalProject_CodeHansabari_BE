package com.cvmento.domain.resume.dto.response;

import java.util.List;

public record ResumeMetadataResponse(
        List<TechStackResponse> techStacks,
        List<EnumOptionResponse> resumeTypes,
        List<EnumOptionResponse> careerTypes,
        List<EnumOptionResponse> degreeLevels,
        List<EnumOptionResponse> proficiencyLevels,
        List<EnumOptionResponse> projectTypes,
        List<EnumOptionResponse> additionalInfoCategories
) {
}