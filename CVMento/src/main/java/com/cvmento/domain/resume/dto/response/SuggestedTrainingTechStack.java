package com.cvmento.domain.resume.dto.response;

import com.cvmento.domain.resume.enums.ProficiencyLevel;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "교육에서 다룬 기술스택")
public record SuggestedTrainingTechStack(

        @Schema(description = "기술스택 ID", example = "1")
        Long techStackId,

        @Schema(description = "기술스택 이름", example = "AWS")
        String techStackName,

        @Schema(description = "숙련도", example = "BEGINNER")
        ProficiencyLevel proficiencyLevel

) {
}