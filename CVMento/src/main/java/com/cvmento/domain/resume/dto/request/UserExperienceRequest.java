package com.cvmento.domain.resume.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

@Schema(description = "사용자 경험 정보 요청")
public record UserExperienceRequest(

        @Schema(description = "경력 구분", example = "EXPERIENCED", allowableValues = {"FRESHMAN", "EXPERIENCED"})
        @NotBlank(message = "경력 구분은 필수입니다.")
        String careerType,

        @Schema(description = "지원 분야", example = "백엔드 개발자")
        @NotBlank(message = "지원 분야는 필수입니다.")
        @Size(max = 100, message = "지원 분야는 100자 이하여야 합니다.")
        String fieldName,

        @Schema(description = "경험 및 경력 사항", 
                example = "네이버에서 2년간 백엔드 개발자로 근무했습니다. Spring Boot를 활용한 REST API 개발과 데이터베이스 설계를 담당했으며, 팀 프로젝트에서 성능 최적화를 통해 응답속도를 30% 향상시켰습니다.")
        @NotBlank(message = "경험 사항은 필수입니다.")
        @Size(min = 50, max = 2000, message = "경험 사항은 50자 이상 2000자 이하여야 합니다.")
        String experiences,

        @Schema(description = "보유 기술스택 목록", example = "[\"Java\", \"Spring Boot\", \"MySQL\", \"Docker\"]")
        List<String> techStacks,

        @Schema(description = "추가 정보 (교육, 자격증, 특이사항 등)", 
                example = "정보처리기사 자격증 보유, AWS 클라우드 실무 경험 1년")
        @Size(max = 1000, message = "추가 정보는 1000자 이하여야 합니다.")
        String additionalInfo

) {
}