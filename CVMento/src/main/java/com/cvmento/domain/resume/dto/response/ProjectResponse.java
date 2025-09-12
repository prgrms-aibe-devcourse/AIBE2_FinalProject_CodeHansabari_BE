package com.cvmento.domain.resume.dto.response;

import java.util.List;

/**
 * 프로젝트 응답.
 *
 * @param careerId 경력 ID
 * @param startDate 시작일
 * @param endDate 종료일
 * @param name 프로젝트명
 * @param description 프로젝트 소개
 * @param detailedDescription 프로젝트 상세소개
 * @param repositoryUrl 저장소 링크
 * @param deployUrl 배포 링크
 * @param projectType 프로젝트 타입
 * @param techStacks 기술스택 목록
 */
public record ProjectResponse(
        Long careerId,
        String startDate,
        String endDate,
        String name,
        String description,
        String detailedDescription,
        String repositoryUrl,
        String deployUrl,
        String projectType,
        List<ProjectTechStackResponse> techStacks
) {
}