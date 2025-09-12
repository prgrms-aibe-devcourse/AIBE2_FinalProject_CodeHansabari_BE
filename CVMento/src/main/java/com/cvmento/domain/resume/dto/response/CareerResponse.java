package com.cvmento.domain.resume.dto.response;

import java.util.List;

/**
 * 경력 응답.
 *
 * @param startDate 시작일
 * @param endDate 종료일
 * @param companyName 회사명
 * @param companyDescription 회사 소개
 * @param departmentPosition 부서명/직책
 * @param mainTasks 주요 업무 및 성과
 * @param techStacks 기술스택 목록
 */
public record CareerResponse(
        String startDate,
        String endDate,
        String companyName,
        String companyDescription,
        String departmentPosition,
        String mainTasks,
        List<CareerTechStackResponse> techStacks
) {
}