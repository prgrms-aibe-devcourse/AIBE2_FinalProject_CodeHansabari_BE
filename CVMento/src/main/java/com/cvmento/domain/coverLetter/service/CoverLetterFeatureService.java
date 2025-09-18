package com.cvmento.domain.coverLetter.service;

import com.cvmento.domain.coverLetter.entity.CrawlCoverLetter;
import com.cvmento.domain.coverLetter.entity.RawCoverLetterFeature;
import com.cvmento.domain.coverLetter.repository.CrawlCoverLetterRepository;
import com.cvmento.domain.coverLetter.repository.RawCoverLetterFeatureRepository;
import com.cvmento.domain.coverLetter.service.FeatureExtraction.FeatureExtractionBatchService;
import com.cvmento.global.exception.customException.FeatureExtractionException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 자소서 특징 추출 서비스
 * - 전체 특징 추출 플로우 조율
 * - 하위 서비스들의 통합 관리
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CoverLetterFeatureService {

    private final CrawlCoverLetterRepository crawlRepository;
    private final RawCoverLetterFeatureRepository rawFeatureRepository;
    private final FeatureExtractionBatchService batchService;

    /**
     * 크롤링된 자소서 데이터에서 특징 추출하여 raw_features 테이블에 저장
     * - 중복 제거 없이 모든 특징을 raw_features 테이블에 저장
     */
    public List<RawCoverLetterFeature> extractFeaturesFromCrawledData() {
        try {
            log.info("크롤링된 자소서 데이터에서 특징 추출 시작");

            List<CrawlCoverLetter> crawledCoverLetters = crawlRepository.findAll();
            if (crawledCoverLetters.isEmpty()) {
                log.warn("특징을 추출할 크롤링 데이터가 없습니다.");
                throw new FeatureExtractionException("특징을 추출할 크롤링 데이터가 없습니다.");
            }
            log.info("총 {}개의 크롤링된 자소서 발견", crawledCoverLetters.size());

            // 기존 raw_features 데이터 삭제 (Repository의 기본 트랜잭션으로 실행)
            rawFeatureRepository.deleteAll();
            log.info("기존 raw_features 데이터 삭제 완료");

            log.info("실시간 API를 사용한 특징 추출 시작");
            List<RawCoverLetterFeature> allRawFeatures = batchService.extractFeaturesWithRealtimeAPIAndSave(crawledCoverLetters);

            log.info("총 {}개의 특징을 raw_features 테이블에 저장 완료", allRawFeatures.size());
            return allRawFeatures;

        } catch (Exception e) {
            log.error("특징 추출 중 오류 발생", e);
            throw new FeatureExtractionException("특징 추출 실패", e);
        }
    }
}
