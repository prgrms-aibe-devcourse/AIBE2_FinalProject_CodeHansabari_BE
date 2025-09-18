package com.cvmento.domain.coverLetter.service.FeatureExtraction;

import com.cvmento.domain.coverLetter.dto.response.FeatureCandidate;
import com.cvmento.domain.coverLetter.entity.CrawlCoverLetter;
import com.cvmento.domain.coverLetter.entity.RawCoverLetterFeature;
import com.cvmento.domain.coverLetter.enums.FeaturesCategory;
import com.cvmento.global.common.util.GeminiResponseParser;
import com.cvmento.global.exception.customException.FeatureExtractionException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 특징 추출 응답 파싱 서비스
 * - Gemini API 응답 파싱
 * - JSON 형식 검증 및 변환
 * - FeatureCandidate 및 RawCoverLetterFeature 변환
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FeatureExtractionParserService {

    private final GeminiResponseParser geminiResponseParser;
    private final ObjectMapper objectMapper;

    // 삭제: parseBatchFeatureResponse - 사용처 없음

    /**
     * 단일 자소서 특징 응답 파싱
     */
    public List<FeatureCandidate> parseFeatureResponse(String response, CrawlCoverLetter coverLetter) {
        try {
            String textContent = geminiResponseParser.extractTextContent(response);
            log.info("자소서 {}에서 텍스트 컨텐츠 추출 완료: {}자",
                    coverLetter.getCoverLetterId(), textContent.length());

            List<FeatureCandidate> candidates = parseFeaturesFromText(textContent);
            
            // crawlCoverLetterId 설정
            List<FeatureCandidate> candidatesWithId = candidates.stream()
                    .map(candidate -> new FeatureCandidate(
                            candidate.featureCategory(),
                            candidate.description(),
                            coverLetter.getCoverLetterId()))
                    .collect(Collectors.toList());

            return candidatesWithId;

        } catch (Exception e) {
            log.error("자소서 {}에서 특징 파싱 실패", coverLetter.getCoverLetterId(), e);
            return new ArrayList<>();
        }
    }

    /**
     * 동적 배치 응답 파싱
     */
    public List<RawCoverLetterFeature> parseDynamicBatchResponse(String response, List<CrawlCoverLetter> batch) {
        List<RawCoverLetterFeature> results = new ArrayList<>();
        
        try {
            // 1. Gemini API 응답에서 실제 JSON 텍스트 추출
            String textContent = geminiResponseParser.extractTextContent(response);
            log.info("Gemini 응답에서 텍스트 컨텐츠 추출 완료: {}자", textContent.length());
            
            // 2. 추출된 JSON 텍스트를 파싱
            JsonNode responseJson = objectMapper.readTree(textContent);
            Iterator<String> fieldNames = responseJson.fieldNames();
            String firstKey = fieldNames.hasNext() ? fieldNames.next() : "없음";
            log.info("JSON 파싱 완료, 첫 번째 키: {}", firstKey);
            
            for (CrawlCoverLetter coverLetter : batch) {
                String key = "cover_letter_" + coverLetter.getCoverLetterId();
                log.debug("자소서 {} 키 검색: {}", coverLetter.getCoverLetterId(), key);
                
                if (responseJson.has(key)) {
                    JsonNode coverLetterData = responseJson.get(key);
                    log.debug("자소서 {} 데이터 발견", coverLetter.getCoverLetterId());
                    
                    // EXPRESSION 특징 파싱
                    if (coverLetterData.has("expression_feature")) {
                        JsonNode expressionFeature = coverLetterData.get("expression_feature");
                        String description = expressionFeature.get("description").asText();
                        results.add(new RawCoverLetterFeature(
                            FeaturesCategory.EXPRESSION, 
                            description, 
                            coverLetter.getCoverLetterId()
                        ));
                        log.debug("EXPRESSION 특징 추가: {}", description.substring(0, Math.min(50, description.length())));
                    }
                    
                    // STRUCTURE 특징 파싱
                    if (coverLetterData.has("structure_feature")) {
                        JsonNode structureFeature = coverLetterData.get("structure_feature");
                        String description = structureFeature.get("description").asText();
                        results.add(new RawCoverLetterFeature(
                            FeaturesCategory.STRUCTURE, 
                            description, 
                            coverLetter.getCoverLetterId()
                        ));
                        log.debug("STRUCTURE 특징 추가: {}", description.substring(0, Math.min(50, description.length())));
                    }
                    
                    // CONTENT 특징 파싱
                    if (coverLetterData.has("content_feature")) {
                        JsonNode contentFeature = coverLetterData.get("content_feature");
                        String description = contentFeature.get("description").asText();
                        results.add(new RawCoverLetterFeature(
                            FeaturesCategory.CONTENT, 
                            description, 
                            coverLetter.getCoverLetterId()
                        ));
                        log.debug("CONTENT 특징 추가 - 길이: {}자", description.length());
                    }
                } else {
                    log.warn("자소서 {}에 대한 키 '{}'를 찾을 수 없음", coverLetter.getCoverLetterId(), key);
                }
            }
            
            log.info("동적 배치 응답 파싱 완료: {}개 특징 추출", results.size());
            return results;

        } catch (Exception e) {
            log.error("동적 배치 응답 파싱 실패: {}", e.getMessage(), e);
            log.error("원본 응답 (처음 500자): {}", response.substring(0, Math.min(500, response.length())));
            throw new FeatureExtractionException("응답 파싱 실패", e);
        }
    }

    /**
     * 배치 특징 텍스트에서 파싱
     */
    // 완전 삭제: parseBatchFeaturesFromText

    /**
     * 단일 자소서 특징 텍스트에서 파싱
     */
    private List<FeatureCandidate> parseFeaturesFromText(String text) {
        try {
            if (!text.trim().startsWith("{")) {
                log.warn("JSON 형식이 아닌 응답: {}", text.substring(0, Math.min(100, text.length())));
                return new ArrayList<>();
            }

            var jsonNode = objectMapper.readTree(text);

            if (!jsonNode.has("features")) {
                log.warn("features 필드를 찾을 수 없음");
                return new ArrayList<>();
            }

            var featuresArray = jsonNode.get("features");
            if (!featuresArray.isArray()) {
                log.warn("features가 배열이 아님");
                return new ArrayList<>();
            }

            List<FeatureCandidate> candidates = new ArrayList<>();
            for (var featureNode : featuresArray) {
                if (featureNode.has("feature_category") && featureNode.has("description")) {
                    String featureCategory = featureNode.get("feature_category").asText();
                    String description = featureNode.get("description").asText();
                    // 단일 자소서의 경우 crawl_cover_letter_id는 자소서 ID로 설정
                    Long crawlCoverLetterId = null; // 나중에 설정

                    candidates.add(new FeatureCandidate(featureCategory, description, crawlCoverLetterId));
                }
            }

            log.info("{}개의 특징 파싱 완료", candidates.size());
            return candidates;

        } catch (Exception e) {
            log.error("특징 JSON 파싱 실패: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * 카테고리 문자열을 enum으로 변환
     */
    // 완전 삭제: convertToCategory - 현재 내부 저장로직 분리로 미사용
}
