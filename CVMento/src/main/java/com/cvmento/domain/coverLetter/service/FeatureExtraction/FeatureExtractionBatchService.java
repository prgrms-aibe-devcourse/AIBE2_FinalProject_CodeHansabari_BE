package com.cvmento.domain.coverLetter.service.FeatureExtraction;

import com.cvmento.domain.coverLetter.client.CoverLetterFeatureLlmFeignClient;
import com.cvmento.domain.coverLetter.constants.GeminiConstants;
import com.cvmento.domain.coverLetter.dto.request.FeatureExtractionSchema;
import com.cvmento.domain.coverLetter.dto.request.GeminiRequest;
import com.cvmento.domain.coverLetter.dto.response.FeatureCandidate;
import com.cvmento.domain.coverLetter.entity.CrawlCoverLetter;
import com.cvmento.domain.coverLetter.entity.RawCoverLetterFeature;
import com.cvmento.domain.coverLetter.repository.RawCoverLetterFeatureRepository;
import com.cvmento.global.exception.customException.FeatureExtractionException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 특징 추출 배치 처리 서비스
 * - 실시간 API를 사용한 배치 특징 추출
 * - 중단/재개 지원
 * - 동적 지연 방식으로 API 응답 시간에 따라 적응형 대기
 * - 배치마다 DB 저장으로 중단 시에도 데이터 보존
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FeatureExtractionBatchService {

    private final CoverLetterFeatureLlmFeignClient coverLetterFeatureLlmFeignClient;
    private final RawCoverLetterFeatureRepository rawFeatureRepository;
    private final FeatureExtractionPromptService promptService;
    private final FeatureExtractionParserService parserService;
    private final FeatureExtractionResumeService resumeService;
    private final FeatureExtractionUtils utils;

    /**
     * 실시간 API를 사용한 특징 추출 및 raw_features 저장 (중단/재개 지원)
     * - 중단된 지점부터 자동 재개
     * - 2개 자소서씩 묶어서 한 번에 처리 (효율성 최적화)
     * - 동적 지연 방식으로 API 응답 시간에 따라 적응형 대기
     * - 배치마다 DB 저장으로 중단 시에도 데이터 보존
     * - 250회 제한 고려한 부분 처리
     */
    public List<RawCoverLetterFeature> extractFeaturesWithRealtimeAPIAndSave(List<CrawlCoverLetter> coverLetters) {
        log.info("실시간 API를 사용한 특징 추출 시작 - 총 {}개 자소서", coverLetters.size());
        
        // 중단/재개 로직: 이미 처리된 자소서 제외
        List<CrawlCoverLetter> remainingCoverLetters = resumeService.getRemainingCoverLetters(coverLetters);
        log.info("중단/재개 확인 완료 - 처리할 자소서: {}개", remainingCoverLetters.size());
        
        // 250회 제한 고려 (무료 계정)
        if (remainingCoverLetters.size() > 250) {
            remainingCoverLetters = remainingCoverLetters.subList(0, 250);
            log.warn("일일 한도 고려하여 250개만 처리합니다. 나머지 {}개는 내일 처리됩니다.", 
                    coverLetters.size() - 250);
        }
        
        // 2개씩 배치로 나누어 처리 (효율성 최적화)
        int batchSize = 2;
        List<RawCoverLetterFeature> allRawFeatures = new ArrayList<>();
        
        // 동적 지연을 위한 변수들
        long baseDelay = 5000; // 기본 5초
        long maxDelay = 30000; // 최대 30초
        long currentDelay = baseDelay;
        int consecutiveErrors = 0;
        int maxConsecutiveErrors = 3;
        
        try {
            for (int i = 0; i < remainingCoverLetters.size(); i += batchSize) {
                int endIndex = Math.min(i + batchSize, remainingCoverLetters.size());
                List<CrawlCoverLetter> batch = remainingCoverLetters.subList(i, endIndex);
                
                log.info("배치 처리 중: {}/{} ({}개 자소서)", 
                        i + batch.size(), remainingCoverLetters.size(), batch.size());
                
                long startTime = System.currentTimeMillis();
                
                try {
                    // 실시간 API로 특징 추출
                    List<RawCoverLetterFeature> batchFeatures = extractFeaturesFromRealtimeBatch(batch);
                    allRawFeatures.addAll(batchFeatures);
                    
                    long responseTime = System.currentTimeMillis() - startTime;
                    log.info("배치 처리 완료: {}개 특징 추출 (응답시간: {}ms)", 
                            batchFeatures.size(), responseTime);
                    
                    // 성공 시 지연 시간 조정
                    consecutiveErrors = 0;
                    currentDelay = resumeService.adjustDelayForSuccess(responseTime, currentDelay, baseDelay, maxDelay);
                    
                    // 배치마다 DB에 저장 (중단 시 데이터 보존)
                    rawFeatureRepository.saveAll(batchFeatures);
                    log.info("배치 {} 특징을 DB에 저장 완료", batchFeatures.size());
                    
                } catch (Exception batchError) {
                    consecutiveErrors++;
                    log.warn("배치 처리 실패 (연속 오류: {}/{}): {}", 
                            consecutiveErrors, maxConsecutiveErrors, batchError.getMessage());
                    
                    // 연속 오류가 3회 이상이면 API 호출 중단
                    if (consecutiveErrors >= maxConsecutiveErrors) {
                        log.error("연속 {}회 오류 발생으로 API 호출을 중단합니다", maxConsecutiveErrors);
                        log.error("현재까지 {}개 특징 추출 완료. 중단된 지점부터 내일 재개 가능합니다.", allRawFeatures.size());
                        break; // 배치 처리 루프 중단
                    }
                    
                    // 연속 오류가 3회 미만일 때만 개별 자소서로 처리 시도
                    if (consecutiveErrors < maxConsecutiveErrors) {
                        log.info("개별 자소서 처리로 전환");
                        for (CrawlCoverLetter coverLetter : batch) {
                            try {
                                List<RawCoverLetterFeature> individualFeatures = 
                                        extractFeaturesFromSingleCoverLetterAndSave(coverLetter);
                                allRawFeatures.addAll(individualFeatures);
                                log.info("개별 처리 완료: 자소서 {} - {}개 특징", 
                                        coverLetter.getCoverLetterId(), individualFeatures.size());
                            } catch (Exception individualError) {
                                log.error("자소서 {} 개별 처리도 실패, 건너뛰기: {}", 
                                        coverLetter.getCoverLetterId(), individualError.getMessage());
                            }
                        }
                    } else {
                        log.warn("연속 오류로 인해 개별 자소서 처리도 건너뜁니다");
                    }
                }
                
                // 다음 배치 처리를 위한 동적 지연
                if (endIndex < remainingCoverLetters.size()) {
                    log.info("다음 배치 처리를 위해 {}ms 대기...", currentDelay);
                    Thread.sleep(currentDelay);
                }
            }
            
            log.info("실시간 API를 통해 총 {}개의 특징을 raw_features 테이블에 저장 완료", allRawFeatures.size());
            return allRawFeatures;
            
        } catch (Exception e) {
            log.error("실시간 특징 추출 실패", e);
            
            // 일일 한도 초과 시 특별 처리
            if (e.getMessage() != null && e.getMessage().contains("quota")) {
                log.error("🚨 Gemini API 일일 요청 한도 초과! 무료 계정은 일일 250회 제한이 있습니다.");
                log.error("💡 해결방법: 1) 유료 계정 업그레이드 2) 내일까지 대기 3) 새 API 키 사용");
                log.info("현재까지 {}개 특징 추출 완료. 내일 이어서 진행 가능합니다.", allRawFeatures.size());
                return allRawFeatures; // 부분 결과 반환
            }
            
            throw new FeatureExtractionException("실시간 특징 추출 실패", e);
        }
    }

    /**
     * 실시간 API를 사용한 배치 특징 추출 (재시도 로직 포함)
     */
    private List<RawCoverLetterFeature> extractFeaturesFromRealtimeBatch(List<CrawlCoverLetter> batch) {
        int maxRetries = 3;
        int retryDelay = 2000; // 2초
        
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                log.info("실시간 배치 처리 시작: {}개 자소서 (시도 {}/{})", batch.size(), attempt, maxRetries);
                
                // 1. 동적 프롬프트 생성
                String prompt = promptService.buildDynamicBatchPrompt(batch);
                
                // 2. 동적 응답 스키마 생성
                Object responseSchema = promptService.buildDynamicResponseSchema(batch);
                
                // 3. Gemini 실시간 API 호출
                String thinkingBudget = utils.selectThinkingBudget(batch.size(), batch.size());
                GeminiRequest request = new GeminiRequest(
                        List.of(new GeminiRequest.Content(
                                List.of(new GeminiRequest.Content.Part(prompt))
                        )),
                        new GeminiRequest.GenerationConfig("0.7", "1024",
                                "application/json", responseSchema, thinkingBudget)
                );
                
                String response = coverLetterFeatureLlmFeignClient.analyzeRaw(GeminiConstants.GEMINI_MODEL, request);
                
                // 4. 동적 응답 파싱
                List<RawCoverLetterFeature> result = parserService.parseDynamicBatchResponse(response, batch);
                log.info("실시간 배치 처리 성공: {}개 특징 추출", result.size());
                return result;
                
            } catch (Exception e) {
                log.warn("실시간 배치 특징 추출 실패 (시도 {}/{}): {}", attempt, maxRetries, e.getMessage());
                
                // 503 Service Unavailable인 경우 재시도
                if (e.getMessage().contains("503") || e.getMessage().contains("overloaded")) {
                    if (attempt < maxRetries) {
                        log.info("{}초 후 재시도합니다...", retryDelay / 1000);
                        try {
                            Thread.sleep(retryDelay);
                            retryDelay *= 2; // 지수 백오프
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                            throw new FeatureExtractionException("재시도 중단됨", ie);
                        }
                        continue;
                    }
                }
                
                // 마지막 시도이거나 503이 아닌 경우
                if (attempt == maxRetries) {
                    log.error("실시간 배치 특징 추출 최종 실패 ({}회 시도)", maxRetries);
                    throw new FeatureExtractionException("실시간 배치 처리 실패", e);
                }
            }
        }
        
        throw new FeatureExtractionException("실시간 배치 처리 실패");
    }

    /**
     * 단일 자소서 특징 추출 및 저장 (개별 처리용)
     */
    private List<RawCoverLetterFeature> extractFeaturesFromSingleCoverLetterAndSave(CrawlCoverLetter coverLetter) {
        try {
            log.info("단일 자소서 {} 특징 추출 시작", coverLetter.getCoverLetterId());
            
            String prompt = promptService.buildFullCoverLetterExtractionPrompt(coverLetter);
            
            String thinkingBudget = utils.selectThinkingBudget(1, 1);
            GeminiRequest request = new GeminiRequest(
                    List.of(new GeminiRequest.Content(
                            List.of(new GeminiRequest.Content.Part(prompt))
                    )),
                    new GeminiRequest.GenerationConfig("0.7", "1024",
                            "application/json", FeatureExtractionSchema.SCHEMA, thinkingBudget)
            );
            
            String response = coverLetterFeatureLlmFeignClient.analyzeRaw(GeminiConstants.GEMINI_MODEL, request);
            
            List<FeatureCandidate> candidates = parserService.parseFeatureResponse(response, coverLetter);
            
            List<RawCoverLetterFeature> rawFeatures = new ArrayList<>();
            for (FeatureCandidate candidate : candidates) {
                var category = utils.convertToCategory(candidate.featureCategory());
                RawCoverLetterFeature rawFeature = new RawCoverLetterFeature(
                    category, candidate.description(), coverLetter.getCoverLetterId());
                rawFeatures.add(rawFeature);
            }
            
            // DB에 저장
            rawFeatureRepository.saveAll(rawFeatures);
            log.info("단일 자소서 {}에서 {}개 특징을 DB에 저장", 
                    coverLetter.getCoverLetterId(), rawFeatures.size());
            
            return rawFeatures;
            
        } catch (Exception e) {
            log.error("단일 자소서 {} 특징 추출 실패", coverLetter.getCoverLetterId(), e);
            throw new FeatureExtractionException("단일 자소서 특징 추출 실패", e);
        }
    }
}


