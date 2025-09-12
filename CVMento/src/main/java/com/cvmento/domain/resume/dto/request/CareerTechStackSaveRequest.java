package com.cvmento.domain.resume.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 경력 기술스택 저장 요청.
 *
 * @param techStackId 기술스택 ID
 */
@Schema(description = "경력 기술스택 저장 요청")
public record CareerTechStackSaveRequest(
        @Schema(description = "기술스택 ID", example = "1")
        Long techStackId,

        @Schema(description = "기술스택 이름 (ID 매핑용)", example = "Java")
        String techStackName
) {
}