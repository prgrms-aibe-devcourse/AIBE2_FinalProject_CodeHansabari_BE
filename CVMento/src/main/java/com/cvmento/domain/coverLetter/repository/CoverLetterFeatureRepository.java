package com.cvmento.domain.coverLetter.repository;

import com.cvmento.domain.coverLetter.entity.CoverLetterFeature;
import com.cvmento.domain.coverLetter.enums.FeaturesCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CoverLetterFeatureRepository extends JpaRepository<CoverLetterFeature,Long> {
    
    /**
     * 특정 카테고리의 특징들을 페이징으로 조회
     */
    Page<CoverLetterFeature> findByFeaturesCategory(FeaturesCategory featuresCategory, Pageable pageable);
    
    /**
     * 특정 카테고리의 특징들 삭제
     */
    void deleteByFeaturesCategory(FeaturesCategory featuresCategory);
    
    /**
     * 특정 카테고리 특징 개수 조회
     */
    long countByFeaturesCategory(FeaturesCategory featuresCategory);
}
