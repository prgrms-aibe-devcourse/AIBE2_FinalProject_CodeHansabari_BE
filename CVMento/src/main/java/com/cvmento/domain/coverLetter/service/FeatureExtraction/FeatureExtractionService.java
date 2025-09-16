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

    /**
     * 테스트용: 배치 특징 추출
     */
    @Transactional(readOnly = true)
    public List<FeatureCandidate> testBatchExtraction(int batchSize) {
        try {
            log.info("테스트용 배치 특징 추출 시작 - 배치 크기: {}", batchSize);

            if (!utils.isValidBatchSize(batchSize)) {
                throw new IllegalArgumentException("배치 크기는 1-10 사이여야 합니다.");
            }

            List<CrawlCoverLetter> coverLetters = crawlRepository.findAll();
            if (coverLetters.isEmpty()) {
                log.warn("크롤링된 자소서가 없습니다.");
                return new ArrayList<>();
            }

            int actualBatchSize = Math.min(batchSize, coverLetters.size());
            List<CrawlCoverLetter> batch = coverLetters.subList(0, actualBatchSize);

            log.info("배치 테스트: {}개 자소서 선택 (요청: {}, 전체: {})",
                    actualBatchSize, batchSize, coverLetters.size());

            List<FeatureCandidate> features = extractFeaturesFromCoverLetters(batch);

            log.info("테스트용 배치 특징 추출 완료: {}개 특징 추출", features.size());
            log.info("테스트용: 특징 추출 완료 (DB 저장 안함)");

            return features;

        } catch (Exception e) {
            log.error("테스트용 배치 특징 추출 중 오류 발생", e);
            throw new FeatureExtractionException("테스트용 배치 특징 추출 실패", e);
        }
    }

    /**
     * 테스트용: 단일 자소서에서 특징 추출
     */
    @Transactional(readOnly = true)
    public List<FeatureCandidate> extractFeaturesFromSingleCoverLetter(Long coverLetterId) {
        try {
            log.info("테스트용: 단일 자소서 {}에서 특징 추출 시작", coverLetterId);

            var coverLetterOpt = crawlRepository.findById(coverLetterId);
            if (coverLetterOpt.isEmpty()) {
                log.warn("자소서 {}를 찾을 수 없습니다.", coverLetterId);
                return new ArrayList<>();
            }

            CrawlCoverLetter coverLetter = coverLetterOpt.get();
            log.info("자소서 {} 조회 완료: {}자", coverLetterId, coverLetter.getText().length());

            List<FeatureCandidate> features = extractFeaturesFromFullCoverLetter(coverLetter);
            log.info("테스트용: {}개 특징 추출 완료", features.size());
            log.info("테스트용: 특징 추출 완료 (DB 저장 안함)");

            return features;

        } catch (Exception e) {
            log.error("테스트용 특징 추출 중 오류 발생", e);
            throw new FeatureExtractionException("테스트용 특징 추출 실패", e);
        }
    }

    /**
     * 배치 단위 처리 (테스트용)
     */
    private List<FeatureCandidate> extractFeaturesFromCoverLetters(List<CrawlCoverLetter> coverLetters) {
        List<FeatureCandidate> allCandidates = new ArrayList<>();

        int batchSize = utils.calculateOptimalBatchSize(coverLetters);

        for (int i = 0; i < coverLetters.size(); i += batchSize) {
            int endIndex = Math.min(i + batchSize, coverLetters.size());
            List<CrawlCoverLetter> batch = coverLetters.subList(i, endIndex);

            log.info("배치 처리 중: {}/{} ({}개 자소서)",
                    i + batch.size(), coverLetters.size(), batch.size());

            try {
                List<FeatureCandidate> batchFeatures = extractFeaturesFromBatch(batch);
                allCandidates.addAll(batchFeatures);

                log.info("배치 처리 완료: {}개 특징 추출", batchFeatures.size());

                if (endIndex < coverLetters.size()) {
                    utils.addDelayBetweenRequests();
                }

            } catch (Exception e) {
                log.error("배치 처리 실패, 개별 처리로 전환", e);

                for (CrawlCoverLetter coverLetter : batch) {
                    try {
                        List<FeatureCandidate> features = extractFeaturesFromFullCoverLetter(coverLetter);
                        allCandidates.addAll(features);
                        log.info("개별 처리 완료: 자소서 {} - {}개 특징",
                                coverLetter.getCoverLetterId(), features.size());
                    } catch (Exception individualError) {
                        log.error("자소서 {} 개별 처리도 실패, 건너뛰기",
                                coverLetter.getCoverLetterId(), individualError);
                    }
                }
            }
        }

        log.info("총 {}개 자소서 처리 완료: {}개 특징 추출", coverLetters.size(), allCandidates.size());
        return allCandidates;
    }

    /**
     * 배치 전송 → LLM 호출 (테스트용)
     */
    private List<FeatureCandidate> extractFeaturesFromBatch(List<CrawlCoverLetter> batch) {
        try {
            log.info("배치 처리 시작: {}개 자소서", batch.size());

            String prompt = promptService.buildDynamicBatchPrompt(batch);
            Object responseSchema = promptService.buildDynamicResponseSchema(batch);

            String thinkingBudget = utils.selectThinkingBudget(batch.size(), batch.size());
            GeminiRequest request = new GeminiRequest(
                    List.of(new GeminiRequest.Content(
                            List.of(new GeminiRequest.Content.Part(prompt))
                    )),
                    new GeminiRequest.GenerationConfig("0.7", "8192",
                            "application/json", responseSchema, thinkingBudget)
            );

            String response = coverLetterFeatureLlmFeignClient.analyzeRaw(GeminiConstants.GEMINI_MODEL, request);

            var rawList = parserService.parseDynamicBatchResponse(response, batch);
            List<FeatureCandidate> candidates = new ArrayList<>();
            for (var rf : rawList) {
                candidates.add(new FeatureCandidate(
                        rf.getFeaturesCategory().name(),
                        rf.getDescription(),
                        rf.getCoverLetterId()
                ));
            }
            return candidates;

        } catch (Exception e) {
            log.error("배치 특징 추출 실패", e);
            throw new FeatureExtractionException("배치 처리 실패", e);
        }
    }

    /**
     * 자소서 전체 1건 전송 → LLM (테스트용)
     */
    private List<FeatureCandidate> extractFeaturesFromFullCoverLetter(CrawlCoverLetter coverLetter) {
        try {
            log.info("자소서 전체를 LLM에 전송하여 특징 추출 시작");

            String prompt = promptService.buildFullCoverLetterExtractionPrompt(coverLetter);

            String thinkingBudget = utils.selectThinkingBudget(1, 1);
            GeminiRequest request = new GeminiRequest(
                    List.of(new GeminiRequest.Content(
                            List.of(new GeminiRequest.Content.Part(prompt))
                    )),
                    new GeminiRequest.GenerationConfig("0.7", "8192",
                            "application/json", FeatureExtractionSchema.SCHEMA, thinkingBudget)
            );

            String response = coverLetterFeatureLlmFeignClient.analyzeRaw(GeminiConstants.GEMINI_MODEL, request);

            return parserService.parseFeatureResponse(response, coverLetter);

        } catch (Exception e) {
            log.error("자소서 전체 특징 추출 실패", e);
            return new ArrayList<>();
        }
    }
}
