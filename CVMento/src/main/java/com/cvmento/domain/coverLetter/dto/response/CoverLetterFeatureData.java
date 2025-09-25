package com.cvmento.domain.coverLetter.dto.response;

import com.cvmento.domain.coverLetter.entity.CoverLetterFeature;
import com.cvmento.domain.coverLetter.enums.FeaturesCategory;

import java.time.LocalDateTime;

/**
 * 추출된 특징 데이터를 위한 DTO
 */
public record CoverLetterFeatureData(
    Long coverLetterFeatureId,
    FeaturesCategory featuresCategory,
    String description,
    Integer duplicateCount,
    Long representativeCoverLetterId,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    /**
     * CoverLetterFeature 엔티티를 DTO로 변환
     */
    public static CoverLetterFeatureData from(CoverLetterFeature feature) {
        return new CoverLetterFeatureData(
            feature.getCoverLetterFeatureId(),
            feature.getFeaturesCategory(),
            feature.getDescription(),
            feature.getDuplicateCount(),
            feature.getRepresentativeCoverLetterId(),
            feature.getCreatedAt(),
            feature.getUpdatedAt()
        );
    }
}