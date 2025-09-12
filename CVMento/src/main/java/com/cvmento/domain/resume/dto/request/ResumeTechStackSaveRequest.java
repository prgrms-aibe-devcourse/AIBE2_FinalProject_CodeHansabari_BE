package com.cvmento.domain.resume.dto.request;

import com.cvmento.domain.resume.enums.ProficiencyLevel;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 기술스택 저장 요청.
 *
 * @param techStackId 기술스택 ID
 * @param proficiencyLevel 숙련도
 */
@Schema(description = "기술스택 저장 요청")
public record ResumeTechStackSaveRequest(
        @Schema(description = "기술스택 ID", example = "1")
        Long techStackId,

        @Schema(description = "숙련도", example = "INTERMEDIATE")
        ProficiencyLevel proficiencyLevel
) {
}