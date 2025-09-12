package com.cvmento.domain.coverLetter.dto.internal;

/**
 * 자소서 특징 정보 DTO
 *
 * @param category    특징 카테고리
 * @param description 상세 설명
 */
public record CoverLetterFeatureDto(
        String category,
        String description
) {}
