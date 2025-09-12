package com.cvmento.domain.resume.dto.response;

import java.util.List;

/**
 * 이력서 메타데이터 응답.
 *
 * @param techStacks 기술스택 목록
 * @param resumeTypes 이력서 타입 목록
 * @param careerTypes 경력 구분 목록
 * @param degreeLevels 학위 수준 목록
 * @param proficiencyLevels 숙련도 목록
 * @param projectTypes 프로젝트 타입 목록
 * @param additionalInfoCategories 기타사항 카테고리 목록
 */
public record ResumeMetadataResponse(
        List<TechStackResponse> techStacks,
        List<EnumOptionResponse> resumeTypes,
        List<EnumOptionResponse> careerTypes,
        List<EnumOptionResponse> degreeLevels,
        List<EnumOptionResponse> proficiencyLevels,
        List<EnumOptionResponse> projectTypes,
        List<EnumOptionResponse> additionalInfoCategories
) {
}