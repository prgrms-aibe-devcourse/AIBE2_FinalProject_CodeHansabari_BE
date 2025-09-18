package com.cvmento.domain.coverLetter.repository;

import com.cvmento.domain.coverLetter.entity.CoverLetterFeature;
import com.cvmento.domain.coverLetter.enums.FeaturesCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CoverLetterFeatureRepository extends JpaRepository<CoverLetterFeature,Long> {
    
    /**
     * 특정 카테고리의 특징들 조회
     */
    List<CoverLetterFeature> findByFeaturesCategory(FeaturesCategory featuresCategory);

    /**
     * 특정 카테고리의 특징들을 페이징으로 조회
     */
    Page<CoverLetterFeature> findByFeaturesCategory(FeaturesCategory featuresCategory, Pageable pageable);
    
    /**
     * 중복횟수 기준 내림차순 정렬 조회
     */
    List<CoverLetterFeature> findAllByOrderByDuplicateCountDesc();
    
    /**
     * 특정 카테고리에서 중복횟수 기준 내림차순 정렬 조회
     */
    List<CoverLetterFeature> findByFeaturesCategoryOrderByDuplicateCountDesc(FeaturesCategory featuresCategory);
    
    /**
     * 중복횟수가 특정 값 이상인 특징들 조회
     */
    List<CoverLetterFeature> findByDuplicateCountGreaterThanEqual(Integer minDuplicateCount);
    
    /**
     * 특정 카테고리의 특징들 삭제
     */
    void deleteByFeaturesCategory(FeaturesCategory featuresCategory);
    
    /**
     * 대표 자소서 ID로 특징 조회
     */
    List<CoverLetterFeature> findByRepresentativeCoverLetterId(Long representativeCoverLetterId);
    
    /**
     * 특정 카테고리에서 대표 자소서 ID로 특징 조회
     */
    List<CoverLetterFeature> findByFeaturesCategoryAndRepresentativeCoverLetterId(FeaturesCategory featuresCategory, Long representativeCoverLetterId);
    
    /**
     * 특정 카테고리 특징 개수 조회
     */
    long countByFeaturesCategory(FeaturesCategory featuresCategory);
}
