package com.cvmento.domain.resume.dto.request;

import com.cvmento.domain.resume.enums.ProjectType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;

@Schema(description = "프로젝트 저장 요청")
public record ProjectSaveRequest(

        @Schema(description = "시작일", example = "2023-03-01")
        @NotNull(message = "시작일은 필수입니다.")
        LocalDate startDate,

        @Schema(description = "종료일", example = "2023-08-31")
        @NotNull(message = "종료일은 필수입니다.")
        LocalDate endDate,

        @Schema(description = "프로젝트명", example = "쇼핑몰 프로젝트")
        @NotBlank(message = "프로젝트명은 필수입니다.")
        String name,

        @Schema(description = "프로젝트 소개", example = "온라인 쇼핑몰 웹사이트")
        String description,

        @Schema(description = "프로젝트 상세소개", example = "Spring Boot와 React를 활용한 쇼핑몰...")
        String detailedDescription,

        @Schema(description = "저장소 링크", example = "https://github.com/user/project")
        String repositoryUrl,

        @Schema(description = "배포 링크", example = "https://project.example.com")
        String deployUrl,

        @Schema(description = "프로젝트 타입", example = "PERSONAL")
        ProjectType projectType,

        @Schema(description = "사용 기술스택 목록")
        @Valid
        List<ProjectTechStackSaveRequest> techStacks
) {
}