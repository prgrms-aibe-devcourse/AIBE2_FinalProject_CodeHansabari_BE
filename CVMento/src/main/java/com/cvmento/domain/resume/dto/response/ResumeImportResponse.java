package com.cvmento.domain.resume.dto.response;

import com.cvmento.domain.resume.dto.request.*;
import com.cvmento.domain.resume.enums.CareerType;
import com.cvmento.domain.resume.enums.ResumeType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * 이력서 파일 변환 응답.
 *
 * @param title 이력서 제목
 * @param type 이력서 타입
 * @param name 이름
 * @param email 이메일
 * @param birthYear 출생년도
 * @param phone 전화번호
 * @param careerType 경력 유형
 * @param fieldName 지원분야
 * @param introduction 간단소개
 * @param githubUrl 깃허브 URL
 * @param blogUrl 블로그 URL
 * @param notionUrl 노션 URL
 * @param educations 학력 정보 목록
 * @param techStacks 기술스택 목록
 * @param customLinks 커스텀 링크 목록
 * @param careers 경력 정보 목록
 * @param projects 프로젝트 정보 목록
 * @param trainings 교육이력 목록
 * @param additionalInfos 기타사항 목록
 */
@Schema(description = "이력서 파일 변환 응답")
public record ResumeImportResponse(
        @Schema(description = "이력서 제목", example = "백엔드 개발자 이력서")
        String title,
        
        @Schema(description = "이력서 타입", example = "DEFAULT")
        ResumeType type,
        
        @Schema(description = "이름", example = "김개발")
        String name,
        
        @Schema(description = "이메일", example = "kim@example.com")
        String email,
        
        @Schema(description = "출생년도", example = "1995")
        Integer birthYear,
        
        @Schema(description = "전화번호", example = "010-1234-5678")
        String phone,
        
        @Schema(description = "경력 유형", example = "EXPERIENCED")
        CareerType careerType,
        
        @Schema(description = "분야/직무", example = "백엔드 개발자")
        String fieldName,
        
        @Schema(description = "자기소개")
        String introduction,
        
        @Schema(description = "GitHub URL")
        String githubUrl,
        
        @Schema(description = "블로그 URL")
        String blogUrl,
        
        @Schema(description = "Notion URL")
        String notionUrl,
        
        @Schema(description = "학력 정보")
        List<EducationSaveRequest> educations,
        
        @Schema(description = "기술 스택")
        List<ResumeTechStackSaveRequest> techStacks,
        
        @Schema(description = "커스텀 링크")
        List<CustomLinkSaveRequest> customLinks,
        
        @Schema(description = "경력 사항")
        List<CareerSaveRequest> careers,
        
        @Schema(description = "프로젝트")
        List<ProjectSaveRequest> projects,
        
        @Schema(description = "교육/훈련")
        List<TrainingSaveRequest> trainings,
        
        @Schema(description = "추가 정보")
        List<AdditionalInfoSaveRequest> additionalInfos
) {
    
    public ResumeSaveRequest toResumeSaveRequest() {
        return new ResumeSaveRequest(
                title, type, name, email, birthYear, phone, careerType, fieldName,
                introduction, githubUrl, blogUrl, notionUrl,
                educations, techStacks, customLinks, careers, projects, trainings, additionalInfos
        );
    }
}