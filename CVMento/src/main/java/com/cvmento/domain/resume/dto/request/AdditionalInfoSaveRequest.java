package com.cvmento.domain.resume.dto.request;

import com.cvmento.domain.resume.enums.AdditionalInfoCategory;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

/**
 * 기타사항 저장 요청.
 *
 * @param startDate 시작일
 * @param endDate 종료일
 * @param category 카테고리
 * @param activityName 활동명
 * @param relatedOrganization 관련기관
 * @param detailedContent 상세내용
 * @param certificateNumber 자격증 번호
 * @param languageLevel 어학 등급
 */
@Schema(description = "기타사항 저장 요청")
public record AdditionalInfoSaveRequest(
        @Schema(description = "시작일", example = "2023-05-01")
        LocalDate startDate,

        @Schema(description = "종료일", example = "2023-05-01")
        LocalDate endDate,

        @Schema(description = "카테고리", example = "AWARD")
        AdditionalInfoCategory category,

        @Schema(description = "활동명", example = "해커톤 대상")
        String activityName,

        @Schema(description = "관련기관", example = "한국정보화진흥원")
        String relatedOrganization,

        @Schema(description = "상세내용", example = "AI 해커톤에서 1등 수상")
        String detailedContent,

        @Schema(description = "자격증 번호", example = "2023-001234")
        String certificateNumber,

        @Schema(description = "어학 등급", example = "TOEIC 900점")
        String languageLevel
) {
}