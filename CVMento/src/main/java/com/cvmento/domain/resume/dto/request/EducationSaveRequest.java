package com.cvmento.domain.resume.dto.request;

import com.cvmento.domain.resume.enums.DegreeLevel;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 학력 저장 요청.
 *
 * @param schoolName 학교명
 * @param major 전공명
 * @param degreeLevel 학위 수준
 * @param personalGpa 개인 학점
 * @param totalGpa 기준 학점
 * @param graduationDate 졸업(예정)일
 */
@Schema(description = "학력 저장 요청")
public record EducationSaveRequest(
        @Schema(description = "학교명", example = "서울대학교")
        String schoolName,

        @Schema(description = "전공명", example = "컴퓨터공학과")
        String major,

        @Schema(description = "학위 수준", example = "BACHELOR")
        DegreeLevel degreeLevel,

        @Schema(description = "개인 학점", example = "3.8")
        BigDecimal personalGpa,

        @Schema(description = "기준 학점", example = "4.5")
        BigDecimal totalGpa,

        @Schema(description = "졸업(예정)일", example = "2024-02-15")
        LocalDate graduationDate
) {
}