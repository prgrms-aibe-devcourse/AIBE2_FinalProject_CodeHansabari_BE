package com.cvmento.domain.resume.dto.request;

import com.cvmento.domain.resume.enums.ResumeType;
import com.cvmento.domain.resume.enums.CareerType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.util.List;

@Schema(description = "이력서 수정 요청")
public record ResumeUpdateRequest(

        // === 기본 정보 ===
        @Schema(description = "이력서 제목", example = "네이버 백엔드 개발자 지원용 이력서 (수정)")
        @NotBlank(message = "이력서 제목은 필수입니다.")
        @Size(max = 100, message = "이력서 제목은 100자 이하여야 합니다.")
        String title,

        @Schema(description = "이력서 타입", example = "MODERN")
        @NotNull(message = "이력서 타입은 필수입니다.")
        ResumeType type,

        @Schema(description = "이름", example = "김개발")
        @NotBlank(message = "이름은 필수입니다.")
        String name,

        @Schema(description = "이메일", example = "kim@example.com")
        @NotBlank(message = "이메일은 필수입니다.")
        @Email(message = "올바른 이메일 형식이 아닙니다.")
        String email,

        @Schema(description = "출생년도", example = "1995")
        @NotNull(message = "출생년도는 필수입니다.")
        @Min(value = 1900, message = "출생년도는 1900년 이후여야 합니다.")
        @Max(value = 2020, message = "출생년도는 2020년 이전이어야 합니다.")
        Integer birthYear,

        @Schema(description = "전화번호", example = "010-1234-5678")
        @NotBlank(message = "전화번호는 필수입니다.")
        String phone,

        @Schema(description = "경력 구분", example = "EXPERIENCED")
        @NotNull(message = "경력 구분은 필수입니다.")
        CareerType careerType,

        @Schema(description = "지원분야", example = "시니어 백엔드 개발자")
        @NotBlank(message = "지원분야는 필수입니다.")
        String fieldName,

        @Schema(description = "간단소개", example = "5년 경력의 백엔드 개발자입니다.")
        String introduction,

        @Schema(description = "깃허브 URL", example = "https://github.com/username")
        String githubUrl,

        @Schema(description = "블로그 URL", example = "https://blog.example.com")
        String blogUrl,

        @Schema(description = "노션 URL", example = "https://notion.so/username")
        String notionUrl,

        // === 상세 정보 ===
        @Schema(description = "학력 정보 목록")
        @Valid
        List<EducationSaveRequest> educations,

        @Schema(description = "기술스택 목록")
        @Valid
        List<ResumeTechStackSaveRequest> techStacks,

        @Schema(description = "커스텀 링크 목록")
        @Valid
        List<CustomLinkSaveRequest> customLinks,

        @Schema(description = "경력 정보 목록")
        @Valid
        List<CareerSaveRequest> careers,

        @Schema(description = "프로젝트 정보 목록")
        @Valid
        List<ProjectSaveRequest> projects,

        @Schema(description = "교육이력 목록")
        @Valid
        List<TrainingSaveRequest> trainings,

        @Schema(description = "기타사항 목록")
        @Valid
        List<AdditionalInfoSaveRequest> additionalInfos
) {
    // ResumeSaveRequest와 동일한 구조로 변환하는 헬퍼 메서드
    public ResumeSaveRequest toSaveRequest() {
        return new ResumeSaveRequest(
                title, type, name, email, birthYear, phone, careerType, fieldName,
                introduction, githubUrl, blogUrl, notionUrl,
                educations, techStacks, customLinks, careers, projects, trainings, additionalInfos
        );
    }
}