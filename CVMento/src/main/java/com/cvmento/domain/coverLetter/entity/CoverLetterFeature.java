package com.cvmento.domain.coverLetter.entity;

import com.cvmento.domain.coverLetter.enums.FeaturesCategory;
import com.cvmento.global.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Getter
@Table(name = "cover_letter_features")
public class CoverLetterFeature extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cover_letter_feature_id")
    private Long coverLetterFeatureId;

    @Column(name = "features_category", nullable = false)
    @Enumerated(EnumType.STRING)
    private FeaturesCategory featuresCategory;

    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "duplicate_count")
    private Integer duplicateCount; // 중복횟수 (원본 클러스터 크기)

    @Column(name = "representative_cover_letter_id", nullable = false)
    private Long representativeCoverLetterId; // 대표 자소서 ID

    protected CoverLetterFeature() {}

    public CoverLetterFeature(FeaturesCategory featuresCategory, String description, 
                             Integer duplicateCount, Long representativeCoverLetterId) {
        this.featuresCategory = featuresCategory;
        this.description = description;
        this.duplicateCount = duplicateCount;
        this.representativeCoverLetterId = representativeCoverLetterId;
    }
}
