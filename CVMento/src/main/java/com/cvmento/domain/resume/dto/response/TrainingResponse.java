package com.cvmento.domain.resume.dto.response;

import java.util.List;

/**
 * 교육이력 응답.
 *
 * @param startDate 시작일
 * @param endDate 종료일
 * @param courseName 교육과정명
 * @param institutionName 교육기관명
 * @param detailedContent 교육 상세내용
 * @param techStacks 배운 기술스택 목록
 */
public record TrainingResponse(
        String startDate,
        String endDate,
        String courseName,
        String institutionName,
        String detailedContent,
        List<TrainingTechStackResponse> techStacks
) {
}