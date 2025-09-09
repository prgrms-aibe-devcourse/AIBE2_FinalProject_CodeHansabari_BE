package com.cvmento.domain.resume.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "경력 기술스택 저장 요청")
public record CareerTechStackSaveRequest(

        @Schema(description = "기술스택 ID", example = "1")
        @NotNull(message = "기술스택 ID는 필수입니다.")
        Long techStackId
) {
}