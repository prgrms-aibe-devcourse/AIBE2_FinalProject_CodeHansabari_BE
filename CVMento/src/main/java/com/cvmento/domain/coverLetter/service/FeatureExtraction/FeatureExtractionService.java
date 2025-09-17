package com.cvmento.domain.coverLetter.service.FeatureExtraction;

import com.cvmento.domain.coverLetter.client.CoverLetterFeatureLlmFeignClient;
import com.cvmento.domain.coverLetter.constants.GeminiConstants;
import com.cvmento.domain.coverLetter.dto.request.FeatureExtractionSchema;
import com.cvmento.domain.coverLetter.dto.request.GeminiRequest;
import com.cvmento.domain.coverLetter.dto.response.FeatureCandidate;
import com.cvmento.domain.coverLetter.entity.CrawlCoverLetter;
import com.cvmento.domain.coverLetter.entity.RawCoverLetterFeature;
import com.cvmento.domain.coverLetter.repository.CrawlCoverLetterRepository;
import com.cvmento.domain.coverLetter.repository.RawCoverLetterFeatureRepository;
import com.cvmento.global.exception.customException.FeatureExtractionException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 특징 추출 메인 서비스
 * - 전체 특징 추출 플로우 조율
 * - 테스트용 특징 추출
 * - 단일 자소서 특징 추출
 * - 하위 서비스들의 통합 관리
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FeatureExtractionService {

    private final CoverLetterFeatureLlmFeignClient coverLetterFeatureLlmFeignClient;
    private final CrawlCoverLetterRepository crawlRepository;
    private final RawCoverLetterFeatureRepository rawFeatureRepository;
    private final FeatureExtractionBatchService batchService;
    private final FeatureExtractionPromptService promptService;
    private final FeatureExtractionParserService parserService;
    private final FeatureExtractionUtils utils;

    /**
     * 크롤링된 자소서 데이터에서 특징 추출하여 raw_features 테이블에 저장
     * - 자소서 1개당 카테고리별 1개씩 총 3개 특징 추출
     * - 중복 제거 없이 모든 특징을 raw_features 테이블에 저장
     */
    @Transactional
    public List<RawCoverLetterFeature> extractFeaturesFromCrawledData(boolean useBatchAPI) {
        try {
            log.info("크롤링된 자소서 데이터에서 특징 추출 시작 - Batch API 사용: {}", useBatchAPI);

            List<CrawlCoverLetter> crawledCoverLetters = crawlRepository.findAll();
            log.info("총 {}개의 크롤링된 자소서 발견", crawledCoverLetters.size());

            // 기존 raw_features 데이터 삭제
            rawFeatureRepository.deleteAll();
            log.info("기존 raw_features 데이터 삭제 완료");

            List<RawCoverLetterFeature> allRawFeatures;

            if (useBatchAPI) {
                log.info("Gemini Batch API를 사용한 특징 추출 시작");
                allRawFeatures = batchService.extractFeaturesWithRealtimeAPIAndSave(crawledCoverLetters);
            } else {
                log.info("실시간 API를 사용한 특징 추출 시작");
                allRawFeatures = batchService.extractFeaturesWithRealtimeAPIAndSave(crawledCoverLetters);
            }

            log.info("총 {}개의 특징을 raw_features 테이블에 저장 완료", allRawFeatures.size());
            return allRawFeatures;

        } catch (Exception e) {
            log.error("특징 추출 중 오류 발생", e);
            throw new FeatureExtractionException("특징 추출 실패", e);
        }
    }

    /**
     * 기본 특징 추출 (실시간 API 사용)
     */
    public List<RawCoverLetterFeature> extractFeaturesFromCrawledData() {
        return extractFeaturesFromCrawledData(false);
    }





}
