package com.cvmento.domain.resume.dto.response;

/**
 * 이력서 기술스택 응답.
 *
 * @param techStackId 기술스택 ID
 * @param techStackName 기술스택명
 * @param category 카테고리
 * @param proficiencyLevel 숙련도
 */
public record ResumeTechStackResponse(
        Long techStackId,
        String techStackName,
        String category,
        String proficiencyLevel
) {
}