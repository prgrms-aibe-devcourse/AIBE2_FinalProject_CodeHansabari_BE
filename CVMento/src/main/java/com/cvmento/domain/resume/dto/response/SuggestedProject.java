package com.cvmento.domain.resume.dto.response;

import com.cvmento.domain.resume.enums.ProjectType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.util.List;

@Schema(description = "AI가 제안하는 프로젝트 정보")
public record SuggestedProject(

        @Schema(description = "시작일", example = "2023-03-01")
        LocalDate startDate,

        @Schema(description = "종료일", example = "2023-08-31")
        LocalDate endDate,

        @Schema(description = "프로젝트명", example = "온라인 쇼핑몰 프로젝트")
        String name,

        @Schema(description = "프로젝트 소개", example = "Spring Boot와 React를 활용한 풀스택 쇼핑몰 웹사이트")
        String description,

        @Schema(description = "프로젝트 상세소개", 
                example = "사용자 인증, 상품 관리, 주문 처리 등 전체 백엔드 API를 설계하고 구현했습니다. 대용량 트래픽 처리를 위해 캐싱을 적용하고 데이터베이스 인덱스를 최적화했습니다.")
        String detailedDescription,

        @Schema(description = "저장소 링크", example = "https://github.com/user/shopping-mall")
        String repositoryUrl,

        @Schema(description = "배포 링크", example = "https://shopping-mall.example.com")
        String deployUrl,

        @Schema(description = "프로젝트 타입", example = "PERSONAL")
        ProjectType projectType,

        @Schema(description = "사용 기술스택")
        List<SuggestedProjectTechStack> techStacks

) {
}