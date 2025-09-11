package com.cvmento.domain.resume.dto.request;

import com.cvmento.domain.resume.enums.ProficiencyLevel;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "기술스택 저장 요청")
public record ResumeTechStackSaveRequest(

        @Schema(description = "기술스택 ID", example = "1")
        Long techStackId,

        @Schema(description = "숙련도", example = "INTERMEDIATE")
        ProficiencyLevel proficiencyLevel
) {
}