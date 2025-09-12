package com.cvmento.domain.resume.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 교육 기술스택 저장 요청.
 *
 * @param techStackId 기술스택 ID
 */
@Schema(description = "교육 기술스택 저장 요청")
public record TrainingTechStackSaveRequest(
        @Schema(description = "기술스택 ID", example = "1")
        Long techStackId
) {
}