package com.cvmento.domain.resume.dto.response;

/**
 * 기타사항 응답.
 *
 * @param startDate 시작일
 * @param endDate 종료일
 * @param category 카테고리
 * @param activityName 활동명
 * @param relatedOrganization 관련기관
 * @param detailedContent 상세내용
 * @param certificateNumber 자격증 번호
 * @param languageLevel 어학 등급
 */
public record AdditionalInfoResponse(
        String startDate,
        String endDate,
        String category,
        String activityName,
        String relatedOrganization,
        String detailedContent,
        String certificateNumber,
        String languageLevel
) {
}