package com.cvmento.domain.resume.dto.request;

import jakarta.validation.constraints.NotBlank;

public record ResumeAiExperienceRequest(
        @NotBlank(message = "경험 내용은 비어있을 수 없습니다.")
        String experienceContent
) {
}
