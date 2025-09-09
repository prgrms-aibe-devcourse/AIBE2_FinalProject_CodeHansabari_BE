package com.cvmento.domain.resume.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "프로젝트 기술스택 저장 요청")
public record ProjectTechStackSaveRequest(

        @Schema(description = "기술스택 ID", example = "1")
        @NotNull(message = "기술스택 ID는 필수입니다.")
        Long techStackId,

        @Schema(description = "사용 용도", example = "백엔드")
        String usageType
) {
}