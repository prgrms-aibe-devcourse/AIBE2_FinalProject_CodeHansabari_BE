package com.cvmento.domain.resume.dto.response;

import java.math.BigDecimal;

public record EducationResponse(
        String schoolName,
        String major,
        String degreeLevel,
        BigDecimal personalGpa,
        BigDecimal totalGpa,
        String graduationDate
) {
}