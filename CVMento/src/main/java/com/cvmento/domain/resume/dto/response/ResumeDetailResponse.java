package com.cvmento.domain.resume.dto.response;

import com.cvmento.domain.resume.enums.ResumeType;
import com.cvmento.domain.resume.enums.CareerType;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 이력서 상세 응답.
 *
 * @param resumeId 이력서 ID
 * @param title 이력서 제목
 * @param type 이력서 타입
 * @param name 이름
 * @param email 이메일
 * @param birthYear 출생년도
 * @param phone 전화번호
 * @param careerType 경력 구분
 * @param fieldName 지원분야
 * @param introduction 간단소개
 * @param githubUrl 깃허브 URL
 * @param blogUrl 블로그 URL
 * @param notionUrl 노션 URL
 * @param createdAt 생성일시
 * @param updatedAt 수정일시
 * @param educations 학력 정보 목록
 * @param techStacks 기술스택 목록
 * @param customLinks 커스텀 링크 목록
 * @param careers 경력 정보 목록
 * @param projects 프로젝트 정보 목록
 * @param trainings 교육이력 목록
 * @param additionalInfos 기타사항 목록
 */
public record ResumeDetailResponse(
        Long resumeId,
        String title,
        ResumeType type,
        String name,
        String email,
        Integer birthYear,
        String phone,
        CareerType careerType,
        String fieldName,
        String introduction,
        String githubUrl,
        String blogUrl,
        String notionUrl,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime createdAt,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime updatedAt,
        List<EducationResponse> educations,
        List<ResumeTechStackResponse> techStacks,
        List<CustomLinkResponse> customLinks,
        List<CareerResponse> careers,
        List<ProjectResponse> projects,
        List<TrainingResponse> trainings,
        List<AdditionalInfoResponse> additionalInfos
) {
}