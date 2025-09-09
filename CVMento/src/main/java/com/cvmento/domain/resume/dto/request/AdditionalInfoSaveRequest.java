package com.cvmento.domain.resume.dto.request;

import com.cvmento.domain.resume.enums.AdditionalInfoCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

@Schema(description = "기타사항 저장 요청")
public record AdditionalInfoSaveRequest(

        @Schema(description = "시작일", example = "2023-05-01")
        @NotNull(message = "시작일은 필수입니다.")
        LocalDate startDate,

        @Schema(description = "종료일", example = "2023-05-01")
        LocalDate endDate,

        @Schema(description = "카테고리", example = "AWARD")
        @NotNull(message = "카테고리는 필수입니다.")
        AdditionalInfoCategory category,

        @Schema(description = "활동명", example = "해커톤 대상")
        @NotBlank(message = "활동명은 필수입니다.")
        String activityName,

        @Schema(description = "관련기관", example = "한국정보화진흥원")
        @NotBlank(message = "관련기관은 필수입니다.")
        String relatedOrganization,

        @Schema(description = "상세내용", example = "AI 해커톤에서 1등 수상")
        String detailedContent,

        @Schema(description = "자격증 번호", example = "2023-001234")
        String certificateNumber,

        @Schema(description = "어학 등급", example = "TOEIC 900점")
        String languageLevel
) {
}