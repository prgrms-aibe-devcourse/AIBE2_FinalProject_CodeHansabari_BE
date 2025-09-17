package com.cvmento.domain.coverLetter.service;

import com.cvmento.domain.coverLetter.dto.response.CoverLetterFeatureData;
import com.cvmento.domain.coverLetter.entity.CoverLetterFeature;
import com.cvmento.domain.coverLetter.enums.FeaturesCategory;
import com.cvmento.domain.coverLetter.repository.CoverLetterFeatureRepository;
import com.cvmento.global.exception.customException.FeatureExtractionException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 추출된 특징 조회 서비스
 * - 페이징을 통한 특징 조회
 * - 카테고리별 특징 조회
 * - 정렬 옵션 제공
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CoverLetterFeatureQueryService {

    private final CoverLetterFeatureRepository coverLetterFeatureRepository;

    /**
     * 모든 특징을 페이징으로 조회 (생성일 기준 내림차순)
     */
    @Transactional(readOnly = true)
    public Page<CoverLetterFeatureData> getAllFeaturesWithPagination(Pageable pageable) {
        MDC.put("spanId", "feature-query-all");
        
        try {
            Page<CoverLetterFeature> featurePage = coverLetterFeatureRepository.findAll(pageable);
            
            log.info("모든 특징 페이징 조회 완료 - 페이지: {}, 크기: {}, 총 개수: {}",
                    pageable.getPageNumber(), pageable.getPageSize(), featurePage.getTotalElements());
            
            return featurePage.map(CoverLetterFeatureData::from);
            
        } catch (Exception e) {
            log.error("모든 특징 페이징 조회 중 오류 발생", e);
            throw new FeatureExtractionException("특징 조회 실패", e);
        }
    }

    /**
     * 특정 카테고리의 특징들을 페이징으로 조회 (생성일 기준 내림차순)
     */
    @Transactional(readOnly = true)
    public Page<CoverLetterFeatureData> getFeaturesByCategoryWithPagination(
            FeaturesCategory category, Pageable pageable) {
        MDC.put("spanId", "feature-query-category");
        
        try {
            Page<CoverLetterFeature> featurePage = coverLetterFeatureRepository
                    .findByFeaturesCategory(category, pageable);
            
            log.info("카테고리별 특징 페이징 조회 완료 - 카테고리: {}, 페이지: {}, 크기: {}, 총 개수: {}",
                    category, pageable.getPageNumber(), pageable.getPageSize(), featurePage.getTotalElements());
            
            return featurePage.map(CoverLetterFeatureData::from);
            
        } catch (Exception e) {
            log.error("카테고리별 특징 페이징 조회 중 오류 발생 - 카테고리: {}", category, e);
            throw new FeatureExtractionException("카테고리별 특징 조회 실패", e);
        }
    }

    /**
     * 중복횟수 기준 내림차순으로 페이징 조회
     */
    @Transactional(readOnly = true)
    public Page<CoverLetterFeatureData> getFeaturesByDuplicateCountWithPagination(Pageable pageable) {
        MDC.put("spanId", "feature-query-duplicate-count");
        
        try {
            Page<CoverLetterFeature> featurePage = coverLetterFeatureRepository
                    .findAll(pageable);
            
            log.info("중복횟수 기준 특징 페이징 조회 완료 - 페이지: {}, 크기: {}, 총 개수: {}",
                    pageable.getPageNumber(), pageable.getPageSize(), featurePage.getTotalElements());
            
            return featurePage.map(CoverLetterFeatureData::from);
            
        } catch (Exception e) {
            log.error("중복횟수 기준 특징 페이징 조회 중 오류 발생", e);
            throw new FeatureExtractionException("중복횟수 기준 특징 조회 실패", e);
        }
    }

    /**
     * 특정 카테고리에서 중복횟수 기준 내림차순으로 페이징 조회
     */
    @Transactional(readOnly = true)
    public Page<CoverLetterFeatureData> getFeaturesByCategoryAndDuplicateCountWithPagination(
            FeaturesCategory category, Pageable pageable) {
        MDC.put("spanId", "feature-query-category-duplicate-count");
        
        try {
            Page<CoverLetterFeature> featurePage = coverLetterFeatureRepository
                    .findByFeaturesCategory(category, pageable);
            
            log.info("카테고리별 중복횟수 기준 특징 페이징 조회 완료 - 카테고리: {}, 페이지: {}, 크기: {}, 총 개수: {}",
                    category, pageable.getPageNumber(), pageable.getPageSize(), featurePage.getTotalElements());
            
            return featurePage.map(CoverLetterFeatureData::from);
            
        } catch (Exception e) {
            log.error("카테고리별 중복횟수 기준 특징 페이징 조회 중 오류 발생 - 카테고리: {}", category, e);
            throw new FeatureExtractionException("카테고리별 중복횟수 기준 특징 조회 실패", e);
        }
    }

    /**
     * 특징 통계 정보 조회
     */
    @Transactional(readOnly = true)
    public FeatureStatistics getFeatureStatistics() {
        MDC.put("spanId", "feature-statistics");
        
        try {
            long totalCount = coverLetterFeatureRepository.count();
            long expressionCount = coverLetterFeatureRepository.countByFeaturesCategory(FeaturesCategory.EXPRESSION);
            long structureCount = coverLetterFeatureRepository.countByFeaturesCategory(FeaturesCategory.STRUCTURE);
            long contentCount = coverLetterFeatureRepository.countByFeaturesCategory(FeaturesCategory.CONTENT);
            
            log.info("특징 통계 조회 완료 - 총 개수: {}, EXPRESSION: {}, STRUCTURE: {}, CONTENT: {}", 
                    totalCount, expressionCount, structureCount, contentCount);
            
            return new FeatureStatistics(totalCount, expressionCount, structureCount, contentCount);
            
        } catch (Exception e) {
            log.error("특징 통계 조회 중 오류 발생", e);
            throw new FeatureExtractionException("특징 통계 조회 실패", e);
        }
    }

    /**
     * 특징 통계 정보를 담는 레코드
     */
    public record FeatureStatistics(
        long totalCount,
        long expressionCount,
        long structureCount,
        long contentCount
    ) {}
}


