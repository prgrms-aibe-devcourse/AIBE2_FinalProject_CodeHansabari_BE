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

    protected CoverLetterFeature() {}

    public CoverLetterFeature(FeaturesCategory featuresCategory, String description) {
        this.featuresCategory = featuresCategory;
        this.description = description;
    }

    public void updateDescription(String description) {
        this.description = description;
    }
}
