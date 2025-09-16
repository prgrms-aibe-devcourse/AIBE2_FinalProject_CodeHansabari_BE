package com.cvmento.domain.coverLetter.service;

import com.cvmento.domain.coverLetter.dto.response.RawCoverLetterFeatureData;
import com.cvmento.domain.coverLetter.dto.response.RawCoverLetterFeaturePageResponse;
import com.cvmento.domain.coverLetter.entity.RawCoverLetterFeature;
import com.cvmento.domain.coverLetter.enums.FeaturesCategory;
import com.cvmento.domain.coverLetter.repository.RawCoverLetterFeatureRepository;
import com.cvmento.global.exception.customException.FeatureExtractionException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class RawCoverLetterFeatureQueryService {

    private final RawCoverLetterFeatureRepository rawCoverLetterFeatureRepository;

    @Transactional(readOnly = true)
    public RawCoverLetterFeaturePageResponse getRawFeaturesPaged(int page, int size) {
        MDC.put("spanId", "raw-feature-query-all");
        try {
            if (size > 100) size = 100;
            Pageable pageable = PageRequest.of(page, size);
            Page<RawCoverLetterFeature> rawPage = rawCoverLetterFeatureRepository.findAllByOrderByCreatedAtDesc(pageable);
            Page<RawCoverLetterFeatureData> dataPage = rawPage.map(RawCoverLetterFeatureData::from);
            return RawCoverLetterFeaturePageResponse.from(dataPage);
        } catch (Exception e) {
            log.error("Raw 특징 페이징 조회 실패", e);
            throw new FeatureExtractionException("Raw 특징 페이징 조회 실패", e);
        } finally {
            MDC.remove("spanId");
        }
    }

    @Transactional(readOnly = true)
    public RawCoverLetterFeaturePageResponse getRawFeaturesByCategoryPaged(FeaturesCategory category, int page, int size) {
        MDC.put("spanId", "raw-feature-query-category");
        try {
            if (size > 100) size = 100;
            Pageable pageable = PageRequest.of(page, size);
            Page<RawCoverLetterFeature> rawPage = rawCoverLetterFeatureRepository
                    .findByFeaturesCategoryOrderByCreatedAtDesc(category, pageable);
            Page<RawCoverLetterFeatureData> dataPage = rawPage.map(RawCoverLetterFeatureData::from);
            return RawCoverLetterFeaturePageResponse.from(dataPage);
        } catch (Exception e) {
            log.error("Raw 특징 카테고리별 페이징 조회 실패 - {}", category, e);
            throw new FeatureExtractionException("Raw 특징 카테고리별 페이징 조회 실패", e);
        } finally {
            MDC.remove("spanId");
        }
    }
}


