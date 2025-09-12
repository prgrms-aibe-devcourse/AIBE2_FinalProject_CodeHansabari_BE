package com.cvmento.domain.resume.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;

import java.time.LocalDate;
import java.util.List;

/**
 * 경력 저장 요청.
 *
 * @param startDate 시작일
 * @param endDate 종료일
 * @param companyName 회사명
 * @param companyDescription 회사 소개
 * @param departmentPosition 부서명/직책
 * @param mainTasks 주요 업무 및 성과
 * @param techStacks 사용 기술스택 목록
 */
@Schema(description = "경력 저장 요청")
public record CareerSaveRequest(
        @Schema(description = "시작일", example = "2022-01-01")
        LocalDate startDate,

        @Schema(description = "종료일", example = "2023-12-31")
        LocalDate endDate,

        @Schema(description = "회사명", example = "네이버")
        String companyName,

        @Schema(description = "회사 소개", example = "대한민국 최대 IT 기업")
        String companyDescription,

        @Schema(description = "부서명/직책", example = "개발팀/주니어 개발자")
        String departmentPosition,

        @Schema(description = "주요 업무 및 성과", example = "REST API 개발 및 성능 최적화")
        String mainTasks,

        @Schema(description = "사용 기술스택 목록")
        @Valid
        List<CareerTechStackSaveRequest> techStacks
) {
}