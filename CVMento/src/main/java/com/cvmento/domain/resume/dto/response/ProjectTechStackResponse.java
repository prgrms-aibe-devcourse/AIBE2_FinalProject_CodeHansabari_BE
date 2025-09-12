package com.cvmento.domain.resume.dto.response;

/**
 * 프로젝트 기술스택 응답.
 *
 * @param techStackId 기술스택 ID
 * @param techStackName 기술스택명
 * @param category 카테고리
 * @param usageType 사용 용도
 */
public record ProjectTechStackResponse(
        Long techStackId,
        String techStackName,
        String category,
        String usageType
) {
}