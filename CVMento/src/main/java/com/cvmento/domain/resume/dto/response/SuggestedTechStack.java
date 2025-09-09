package com.cvmento.domain.resume.dto.response;

import com.cvmento.domain.resume.enums.ProficiencyLevel;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "AI가 제안하는 기술스택 정보")
public record SuggestedTechStack(

        @Schema(description = "기술스택 ID", example = "1")
        Long techStackId,

        @Schema(description = "기술스택 이름", example = "Java")
        String techStackName,

        @Schema(description = "기술스택 카테고리", example = "Programming Language")
        String category,

        @Schema(description = "숙련도", example = "INTERMEDIATE")
        ProficiencyLevel proficiencyLevel,

        @Schema(description = "사용 경험 설명", example = "2년간 Spring Boot 프레임워크를 활용한 백엔드 개발 경험")
        String experienceDescription

) {
}