package com.cvmento.domain.coverLetter.dto.response;

import com.cvmento.domain.coverLetter.entity.RawCoverLetterFeature;
import com.cvmento.domain.coverLetter.enums.FeaturesCategory;

import java.time.LocalDateTime;

/**
 * Raw 특징 데이터 DTO
 */
public record RawCoverLetterFeatureData(
    Long rawCoverLetterFeatureId,
    FeaturesCategory featuresCategory,
    String description,
    Long coverLetterId,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public static RawCoverLetterFeatureData from(RawCoverLetterFeature feature) {
        return new RawCoverLetterFeatureData(
            feature.getRawCoverLetterFeatureId(),
            feature.getFeaturesCategory(),
            feature.getDescription(),
            feature.getCoverLetterId(),
            feature.getCreatedAt(),
            feature.getUpdatedAt()
        );
    }
}