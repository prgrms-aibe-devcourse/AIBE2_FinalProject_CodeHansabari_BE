package com.cvmento.domain.resume.dto.response;

import com.cvmento.domain.resume.enums.ProficiencyLevel;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "경력에서 사용한 기술스택")
public record SuggestedCareerTechStack(

        @Schema(description = "기술스택 ID", example = "1")
        Long techStackId,

        @Schema(description = "기술스택 이름", example = "Java")
        String techStackName,

        @Schema(description = "숙련도", example = "INTERMEDIATE")
        ProficiencyLevel proficiencyLevel

) {
}