package com.cvmento.domain.resume.dto.response;

public record EducationResponse(
        String schoolName,
        String major,
        String degreeLevel,
        Double personalGpa,
        Double totalGpa,
        String graduationDate
) {
}