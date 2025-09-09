package com.cvmento.domain.resume.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;

import java.time.LocalDate;
import java.util.List;

@Schema(description = "교육이력 저장 요청")
public record TrainingSaveRequest(

        @Schema(description = "시작일", example = "2023-01-15")
        LocalDate startDate,

        @Schema(description = "종료일", example = "2023-06-15")
        LocalDate endDate,

        @Schema(description = "교육과정명", example = "Spring Boot 심화과정")
        String courseName,

        @Schema(description = "교육기관명", example = "패스트캠퍼스")
        String institutionName,

        @Schema(description = "교육 상세내용", example = "Spring Boot, JPA, REST API 개발 실습")
        String detailedContent,

        @Schema(description = "배운 기술스택 목록")
        @Valid
        List<TrainingTechStackSaveRequest> techStacks
) {
}