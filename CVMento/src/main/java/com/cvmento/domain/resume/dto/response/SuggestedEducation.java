package com.cvmento.domain.resume.dto.response;

import com.cvmento.domain.resume.enums.DegreeLevel;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

@Schema(description = "AI가 제안하는 학력 정보")
public record SuggestedEducation(

        @Schema(description = "학교명", example = "서울대학교")
        String schoolName,

        @Schema(description = "전공명", example = "컴퓨터공학과")
        String major,

        @Schema(description = "학위 수준", example = "BACHELOR")
        DegreeLevel degreeLevel,

        @Schema(description = "개인 학점", example = "3.8")
        Double personalGpa,

        @Schema(description = "기준 학점", example = "4.5")
        Double totalGpa,

        @Schema(description = "졸업(예정)일", example = "2024-02-15")
        LocalDate graduationDate

) {
}