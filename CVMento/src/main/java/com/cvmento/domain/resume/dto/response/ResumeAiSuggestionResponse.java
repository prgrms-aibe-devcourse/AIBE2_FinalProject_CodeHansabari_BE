package com.cvmento.domain.resume.dto.response;

import java.util.List;

public record ResumeAiSuggestionResponse(
        List<SuggestedResumeSectionDto> suggestedSections
) {
}
