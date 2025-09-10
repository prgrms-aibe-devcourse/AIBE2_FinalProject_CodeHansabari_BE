package com.cvmento.domain.resume.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "사용자 경험 정보 요청")
public record UserExperienceRequest(

        @Schema(description = "경험 및 경력 사항", 
                example = "네이버에서 2년간 백엔드 개발자로 근무했습니다. Spring Boot를 활용한 REST API 개발과 데이터베이스 설계를 담당했으며, 팀 프로젝트에서 성능 최적화를 통해 응답속도를 30% 향상시켰습니다. 정보처리기사 자격증도 보유하고 있으며, AWS 클라우드 실무 경험도 1년 정도 있습니다.")
        @NotBlank(message = "경험 사항은 필수입니다.")
        @Size(min = 50, max = 3000, message = "경험 사항은 50자 이상 3000자 이하여야 합니다.")
        String experiences

) {
}