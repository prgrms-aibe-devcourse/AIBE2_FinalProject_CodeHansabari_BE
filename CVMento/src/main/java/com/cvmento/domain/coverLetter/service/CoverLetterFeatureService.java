package com.cvmento.domain.coverLetter.service;

import com.cvmento.domain.coverLetter.dto.response.FeatureCandidate;
import com.cvmento.domain.coverLetter.entity.RawCoverLetterFeature;
import com.cvmento.domain.coverLetter.service.FeatureExtraction.FeatureExtractionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 자소서 특징 추출 서비스 (리팩토링됨)
 * - FeatureExtraction 서비스들을 통한 특징 추출
 * - 기존 API 호환성 유지
 * - 새로운 모듈화된 구조 활용
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CoverLetterFeatureService {

    private final FeatureExtractionService featureExtractionService;

    /**
     * 테스트용: 배치 특징 추출
     */
    @Transactional(readOnly = true)
    public List<FeatureCandidate> testBatchExtraction(int batchSize) {
        return featureExtractionService.testBatchExtraction(batchSize);
    }

    /**
     * 테스트용: 단일 자소서에서 특징 추출
     */
    @Transactional(readOnly = true)
    public List<FeatureCandidate> extractFeaturesFromSingleCoverLetter(Long coverLetterId) {
        return featureExtractionService.extractFeaturesFromSingleCoverLetter(coverLetterId);
    }

    /**
     * 크롤링된 자소서 데이터에서 특징 추출하여 raw_features 테이블에 저장
     * - 자소서 1개당 카테고리별 1개씩 총 3개 특징 추출
     * - 중복 제거 없이 모든 특징을 raw_features 테이블에 저장
     */
    @Transactional
    public List<RawCoverLetterFeature> extractFeaturesFromCrawledData(boolean useBatchAPI) {
        return featureExtractionService.extractFeaturesFromCrawledData(useBatchAPI);
    }

    /**
     * 기본 특징 추출 (실시간 API 사용)
     */
    public List<RawCoverLetterFeature> extractFeaturesFromCrawledData() {
        return featureExtractionService.extractFeaturesFromCrawledData();
    }

}
