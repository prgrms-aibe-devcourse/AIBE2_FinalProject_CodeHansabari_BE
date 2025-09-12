package com.cvmento.domain.resume.dto.response;

import java.math.BigDecimal;

/**
 * 학력 응답.
 *
 * @param schoolName 학교명
 * @param major 전공명
 * @param degreeLevel 학위 수준
 * @param personalGpa 개인 학점
 * @param totalGpa 기준 학점
 * @param graduationDate 졸업(예정)일
 */
public record EducationResponse(
        String schoolName,
        String major,
        String degreeLevel,
        BigDecimal personalGpa,
        BigDecimal totalGpa,
        String graduationDate
) {
}