package com.cvmento.domain.coverLetter.entity;

import com.cvmento.domain.coverLetter.enums.FeaturesCategory;
import com.cvmento.global.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.Getter;

/**
 * LLM으로 추출된 원본 특징 데이터를 저장하는 엔티티
 * - 자소서 1개당 카테고리별 1개씩 총 3개 특징 추출
 * - 중복 제거 전의 모든 특징을 보존
 */
@Entity
@Getter
@Table(name = "raw_cover_letter_features")
public class RawCoverLetterFeature extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "raw_cover_letter_feature_id")
    private Long rawCoverLetterFeatureId;

    @Column(name = "features_category", nullable = false)
    @Enumerated(EnumType.STRING)
    private FeaturesCategory featuresCategory;

    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "cover_letter_id", nullable = false)
    private Long coverLetterId; // 어떤 자소서에서 추출되었는지 추적

    protected RawCoverLetterFeature() {}

    public RawCoverLetterFeature(FeaturesCategory featuresCategory, String description, Long coverLetterId) {
        this.featuresCategory = featuresCategory;
        this.description = description;
        this.coverLetterId = coverLetterId;
    }

    public void updateDescription(String description) {
        this.description = description;
    }
}
