package com.cvmento.domain.resume.dto.response;

import com.cvmento.domain.resume.enums.ResumeSectionType;
import java.util.List;

public record SuggestedResumeSectionDto(
        ResumeSectionType sectionType,
        String sectionTitle,
        List<SuggestedResumeItemDto> items
) {
}
