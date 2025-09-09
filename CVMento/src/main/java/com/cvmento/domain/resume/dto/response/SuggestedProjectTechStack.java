package com.cvmento.domain.resume.dto.response;

import com.cvmento.domain.resume.enums.ProficiencyLevel;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "프로젝트에서 사용한 기술스택")
public record SuggestedProjectTechStack(

        @Schema(description = "기술스택 ID", example = "1")
        Long techStackId,

        @Schema(description = "기술스택 이름", example = "React")
        String techStackName,

        @Schema(description = "숙련도", example = "ADVANCED")
        ProficiencyLevel proficiencyLevel

) {
}