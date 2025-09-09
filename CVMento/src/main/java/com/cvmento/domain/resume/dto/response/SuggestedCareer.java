package com.cvmento.domain.resume.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.util.List;

@Schema(description = "AI가 제안하는 경력 정보")
public record SuggestedCareer(

        @Schema(description = "시작일", example = "2022-01-01")
        LocalDate startDate,

        @Schema(description = "종료일", example = "2023-12-31")
        LocalDate endDate,

        @Schema(description = "회사명", example = "네이버")
        String companyName,

        @Schema(description = "회사 소개", example = "대한민국 최대 IT 기업")
        String companyDescription,

        @Schema(description = "부서명/직책", example = "개발팀/백엔드 개발자")
        String departmentPosition,

        @Schema(description = "주요 업무 및 성과", 
                example = "Spring Boot를 활용한 REST API 개발 담당. 데이터베이스 쿼리 최적화를 통해 응답속도 30% 개선. 팀 내 코드 리뷰 프로세스 도입으로 버그 발생률 40% 감소.")
        String mainTasks,

        @Schema(description = "사용 기술스택")
        List<SuggestedCareerTechStack> techStacks

) {
}