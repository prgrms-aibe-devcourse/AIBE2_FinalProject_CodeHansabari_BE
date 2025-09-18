package com.cvmento.domain.coverLetter.repository;

import com.cvmento.domain.coverLetter.entity.RawCoverLetterFeature;
import com.cvmento.domain.coverLetter.enums.FeaturesCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RawCoverLetterFeatureRepository extends JpaRepository<RawCoverLetterFeature, Long> {
    
    /**
     * 특정 카테고리의 모든 특징 조회
     */
    List<RawCoverLetterFeature> findByFeaturesCategory(FeaturesCategory featuresCategory);

    /**
     * 특정 카테고리의 모든 특징 페이징 조회
     */
    Page<RawCoverLetterFeature> findByFeaturesCategory(FeaturesCategory featuresCategory, Pageable pageable);
    
    /**
     * 특정 자소서에서 추출된 특징들 조회
     */
    List<RawCoverLetterFeature> findByCoverLetterId(Long coverLetterId);
    
    /**
     * 모든 특징의 설명만 조회 (임베딩 계산용)
     */
    @Query("SELECT r.description FROM RawCoverLetterFeature r ORDER BY r.rawCoverLetterFeatureId")
    List<String> findAllDescriptions();
    
    /**
     * 특정 카테고리의 특징 설명만 조회
     */
    @Query("SELECT r.description FROM RawCoverLetterFeature r WHERE r.featuresCategory = :category ORDER BY r.rawCoverLetterFeatureId")
    List<String> findDescriptionsByCategory(@Param("category") FeaturesCategory category);
    
    /**
     * 전체 특징 개수 조회
     */
    long count();
    
    /**
     * 특정 카테고리 특징 개수 조회
     */
    long countByFeaturesCategory(FeaturesCategory featuresCategory);
    
    /**
     * 최소 coverLetterId 조회 (재개 지점 확인용)
     */
    @Query("SELECT MIN(r.coverLetterId) FROM RawCoverLetterFeature r")
    Long findMinCoverLetterId();
}
