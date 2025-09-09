package com.cvmento.domain.resume.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * LLM이 제안하는 이력서 정보 (JSON 파싱용)
 */
@Schema(description = "LLM이 제안하는 이력서 정보")
public record LlmSuggestedResume(
        
        @JsonProperty("title")
        @Schema(description = "이력서 제목")
        String title,
        
        @JsonProperty("type")
        @Schema(description = "이력서 타입")
        String type,
        
        @JsonProperty("name")
        @Schema(description = "이름")
        String name,
        
        @JsonProperty("email")
        @Schema(description = "이메일")
        String email,
        
        @JsonProperty("birthYear")
        @Schema(description = "출생년도")
        Integer birthYear,
        
        @JsonProperty("phone")
        @Schema(description = "연락처")
        String phone,
        
        @JsonProperty("careerType")
        @Schema(description = "경력구분")
        String careerType,
        
        @JsonProperty("fieldName")
        @Schema(description = "지원분야")
        String fieldName,
        
        @JsonProperty("introduction")
        @Schema(description = "자기소개")
        String introduction,
        
        @JsonProperty("githubUrl")
        @Schema(description = "깃허브 URL")
        String githubUrl,
        
        @JsonProperty("blogUrl")
        @Schema(description = "블로그 URL")
        String blogUrl,
        
        @JsonProperty("notionUrl")
        @Schema(description = "노션 URL")
        String notionUrl,
        
        @JsonProperty("educations")
        @Schema(description = "학력 정보")
        List<LlmEducation> educations,
        
        @JsonProperty("techStacks")
        @Schema(description = "기술스택 정보")
        List<LlmTechStack> techStacks,
        
        @JsonProperty("careers")
        @Schema(description = "경력 정보")
        List<LlmCareer> careers,
        
        @JsonProperty("projects")
        @Schema(description = "프로젝트 정보")
        List<LlmProject> projects,
        
        @JsonProperty("trainings")
        @Schema(description = "교육 정보")
        List<LlmTraining> trainings,
        
        @JsonProperty("additionalInfos")
        @Schema(description = "기타사항 정보")
        List<LlmAdditionalInfo> additionalInfos
        
) {
    
    // 내부 파싱용 레코드들
    public record LlmEducation(
            @JsonProperty("schoolName") String schoolName,
            @JsonProperty("major") String major,
            @JsonProperty("degreeLevel") String degreeLevel,
            @JsonProperty("personalGpa") Double personalGpa,
            @JsonProperty("totalGpa") Double totalGpa,
            @JsonProperty("graduationDate") String graduationDate
    ) {}
    
    public record LlmTechStack(
            @JsonProperty("techStackId") Long techStackId,
            @JsonProperty("techStackName") String techStackName,
            @JsonProperty("category") String category,
            @JsonProperty("proficiencyLevel") String proficiencyLevel,
            @JsonProperty("experienceDescription") String experienceDescription
    ) {}
    
    public record LlmCareer(
            @JsonProperty("startDate") String startDate,
            @JsonProperty("endDate") String endDate,
            @JsonProperty("companyName") String companyName,
            @JsonProperty("companyDescription") String companyDescription,
            @JsonProperty("departmentPosition") String departmentPosition,
            @JsonProperty("mainTasks") String mainTasks,
            @JsonProperty("techStacks") List<LlmCareerTechStack> techStacks
    ) {}
    
    public record LlmCareerTechStack(
            @JsonProperty("techStackId") Long techStackId,
            @JsonProperty("techStackName") String techStackName,
            @JsonProperty("proficiencyLevel") String proficiencyLevel
    ) {}
    
    public record LlmProject(
            @JsonProperty("startDate") String startDate,
            @JsonProperty("endDate") String endDate,
            @JsonProperty("name") String name,
            @JsonProperty("description") String description,
            @JsonProperty("detailedDescription") String detailedDescription,
            @JsonProperty("repositoryUrl") String repositoryUrl,
            @JsonProperty("deployUrl") String deployUrl,
            @JsonProperty("projectType") String projectType,
            @JsonProperty("techStacks") List<LlmProjectTechStack> techStacks
    ) {}
    
    public record LlmProjectTechStack(
            @JsonProperty("techStackId") Long techStackId,
            @JsonProperty("techStackName") String techStackName,
            @JsonProperty("proficiencyLevel") String proficiencyLevel
    ) {}
    
    public record LlmTraining(
            @JsonProperty("name") String name,
            @JsonProperty("institution") String institution,
            @JsonProperty("startDate") String startDate,
            @JsonProperty("endDate") String endDate,
            @JsonProperty("description") String description
    ) {}
    
    public record LlmAdditionalInfo(
            @JsonProperty("category") String category,
            @JsonProperty("title") String title,
            @JsonProperty("content") String content,
            @JsonProperty("achievementDate") String achievementDate,
            @JsonProperty("description") String description
    ) {}
}