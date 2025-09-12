package com.cvmento.domain.resume.dto.response;

/**
 * 경력 기술스택 응답.
 *
 * @param techStackId 기술스택 ID
 * @param techStackName 기술스택명
 * @param category 카테고리
 */
public record CareerTechStackResponse(
        Long techStackId,
        String techStackName,
        String category
) {
}