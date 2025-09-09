package com.cvmento.domain.resume.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.util.List;

@Schema(description = "AI가 제안하는 교육이력 정보")
public record SuggestedTraining(

        @Schema(description = "시작일", example = "2023-01-01")
        LocalDate startDate,

        @Schema(description = "종료일", example = "2023-03-31")
        LocalDate endDate,

        @Schema(description = "교육명", example = "AWS 클라우드 전문가 과정")
        String name,

        @Schema(description = "교육 기관", example = "한국 AWS 교육원")
        String institution,

        @Schema(description = "교육 내용", example = "AWS 클라우드 인프라 구축 및 운영")
        String description,

        @Schema(description = "사용 기술스택")
        List<SuggestedTrainingTechStack> techStacks

) {
}