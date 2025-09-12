package com.cvmento.domain.coverLetter.service;

import com.cvmento.domain.coverLetter.client.CoverLetterFeatureLlmFeignClient;
import com.cvmento.domain.coverLetter.dto.request.GeminiRequest;
import com.cvmento.domain.coverLetter.dto.response.FeatureCandidate;
import com.cvmento.domain.coverLetter.entity.CrawlCoverLetter;
import com.cvmento.domain.coverLetter.entity.CoverLetterFeature;
import com.cvmento.domain.coverLetter.enums.FeaturesCategory;
import com.cvmento.domain.coverLetter.repository.CrawlCoverLetterRepository;
import com.cvmento.domain.coverLetter.repository.CoverLetterFeatureRepository;
import com.cvmento.global.common.util.GeminiResponseParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class CoverLetterFeatureService {

    private final CoverLetterFeatureLlmFeignClient coverLetterFeatureLlmFeignClient;
    private final CrawlCoverLetterRepository crawlRepository;
    private final CoverLetterFeatureRepository featureRepository;
    private final ObjectMapper objectMapper;
    private final GeminiResponseParser geminiResponseParser;

    /**
     * 테스트용: 단일 자소서에서 특징을 추출하는 메서드
     * @param essayId 추출할 자소서의 ID
     * @return 추출된 특징 후보 리스트 (각 카테고리별 1개씩, 총 3개)
     */
    public List<FeatureCandidate> extractFeaturesFromSingleEssay(Long essayId) {
        try {
            log.info("테스트용: 단일 자소서 {}에서 특징 추출 시작", essayId);
            
            // 1. 특정 자소서 조회
            Optional<CrawlCoverLetter> essayOpt = crawlRepository.findById(essayId);
            if (essayOpt.isEmpty()) {
                log.warn("자소서 {}를 찾을 수 없습니다.", essayId);
                return new ArrayList<>();
            }
            
            CrawlCoverLetter essay = essayOpt.get();
            log.info("자소서 {} 조회 완료: {}자", essayId, essay.getText().length());
            
            // 2. 자소서 전체를 한 번에 LLM에 전송
            List<FeatureCandidate> features = extractFeaturesFromFullEssay(essay);
            log.info("테스트용: {}개 특징 추출 완료", features.size());
            
            // 3. DB 저장은 하지 않음 (테스트용)
            log.info("테스트용: 특징 추출 완료 (DB 저장 안함)");
            
            return features;
            
        } catch (Exception e) {
            log.error("테스트용 특징 추출 중 오류 발생", e);
            throw new RuntimeException("테스트용 특징 추출 실패", e);
        }
    }

    /**
     * 크롤링된 자소서 데이터에서 특징을 추출하는 메인 메서드 (청킹 없이)
     */
    public List<FeatureCandidate> extractFeaturesFromCrawledData() {
        try {
            log.info("크롤링된 자소서 데이터에서 특징 추출 시작 (청킹 없이)");
            
            // 1. 크롤링된 자소서 데이터 조회
            List<CrawlCoverLetter> crawledEssays = crawlRepository.findAll();
            log.info("총 {}개의 크롤링된 자소서 발견", crawledEssays.size());
            
            // 2. 각 자소서에서 특징 추출 (순차 처리)
            List<FeatureCandidate> allCandidates = extractFeaturesFromEssays(crawledEssays);
            log.info("총 {}개의 특징 후보 추출", allCandidates.size());
            
            // 3. 중복 제거 및 병합
            List<FeatureCandidate> deduplicatedFeatures = deduplicateFeatures(allCandidates);
            log.info("중복 제거 후 {}개의 특징", deduplicatedFeatures.size());
            
            // 4. 최종 100개 선정
            List<FeatureCandidate> finalFeatures = selectTop100Features(deduplicatedFeatures);
            log.info("최종 {}개의 특징 선정 완료", finalFeatures.size());
            
            // 5. DB에 저장
            saveFeaturesToDatabase(finalFeatures);
            log.info("{}개의 특징을 DB에 저장 완료", finalFeatures.size());
            
            return finalFeatures;
            
        } catch (Exception e) {
            log.error("특징 추출 중 오류 발생", e);
            throw new RuntimeException("특징 추출 실패", e);
        }
    }

    /**
     * 배치 단위로 자소서들을 처리하여 특징 추출
     */
    private List<FeatureCandidate> extractFeaturesFromEssays(List<CrawlCoverLetter> essays) {
        List<FeatureCandidate> allCandidates = new ArrayList<>();
        
        // 배치 크기 설정 (토큰 제한 고려하여 동적 조정)
        int batchSize = calculateOptimalBatchSize(essays);
        
        for (int i = 0; i < essays.size(); i += batchSize) {
            int endIndex = Math.min(i + batchSize, essays.size());
            List<CrawlCoverLetter> batch = essays.subList(i, endIndex);
            
            log.info("배치 처리 중: {}/{} ({}개 자소서)", 
                    i + batch.size(), essays.size(), batch.size());
            
            try {
                // 배치 단위로 특징 추출
                List<FeatureCandidate> batchFeatures = extractFeaturesFromBatch(batch);
                allCandidates.addAll(batchFeatures);
                
                log.info("배치 처리 완료: {}개 특징 추출", batchFeatures.size());
                
                // 배치 간 지연 (API 할당량 초과 방지)
                if (endIndex < essays.size()) {
                    addDelayBetweenRequests();
                }
                
            } catch (Exception e) {
                log.error("배치 처리 실패, 개별 처리로 전환", e);
                
                // 배치 실패 시 개별 처리로 폴백
                for (CrawlCoverLetter essay : batch) {
                    try {
                        List<FeatureCandidate> features = extractFeaturesFromFullEssay(essay);
                        allCandidates.addAll(features);
                        log.info("개별 처리 완료: 자소서 {} - {}개 특징", essay.getCoverLetterId(), features.size());
                    } catch (Exception individualError) {
                        log.error("자소서 {} 개별 처리도 실패, 건너뛰기", essay.getCoverLetterId(), individualError);
                    }
                }
            }
        }
        
        log.info("총 {}개 자소서 처리 완료: {}개 특징 추출", essays.size(), allCandidates.size());
        return allCandidates;
    }

    /**
     * 요청 간 지연 시간 추가 (API 할당량 초과 방지)
     */
    private void addDelayBetweenRequests() {
        try {
            Thread.sleep(2000); // 2초 대기
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("요청 간 지연이 중단되었습니다.");
        }
    }

    /**
     * 토큰 제한을 고려한 최적 배치 크기 계산
     */
    private int calculateOptimalBatchSize(List<CrawlCoverLetter> essays) {
        if (essays.isEmpty()) return 1;
        
        // 평균 자소서 길이 계산
        double avgLength = essays.stream()
            .mapToInt(essay -> essay.getText().length())
            .average()
            .orElse(1000.0);
        
        // 토큰 제한 고려 (Gemini 2.5 Flash: 8192 토큰)
        // 프롬프트 + 자소서 내용 + 응답을 고려하여 안전 마진 확보
        int maxTokensPerBatch = 6000; // 8192 - 2000 (안전 마진)
        int tokensPerEssay = (int) (avgLength * 1.5); // 자소서 + 프롬프트 오버헤드
        
        int optimalBatchSize = Math.max(1, maxTokensPerBatch / tokensPerEssay);
        
        // 최대 10개, 최소 1개로 제한
        optimalBatchSize = Math.min(10, Math.max(1, optimalBatchSize));
        
        log.info("최적 배치 크기 계산: 평균 자소서 길이 {}자, 배치 크기 {}개", 
                (int) avgLength, optimalBatchSize);
        
        return optimalBatchSize;
    }

    /**
     * 배치 단위로 여러 자소서를 한 번에 LLM에 전송하여 특징 추출
     */
    private List<FeatureCandidate> extractFeaturesFromBatch(List<CrawlCoverLetter> batch) {
        try {
            log.info("배치 처리 시작: {}개 자소서", batch.size());
            
            // 1. 배치용 프롬프트 생성
            String prompt = buildBatchExtractionPrompt(batch);
            
            // 2. LLM API 호출
            GeminiRequest request = new GeminiRequest(
                List.of(new GeminiRequest.Content(
                    List.of(new GeminiRequest.Content.Part(prompt))
                )),
                new GeminiRequest.GenerationConfig("0.7", "8192")
            );
            
            String response = coverLetterFeatureLlmFeignClient.analyzeRaw(request);
            
            // 3. 응답 파싱
            return parseBatchFeatureResponse(response, batch);
            
        } catch (Exception e) {
            log.error("배치 특징 추출 실패", e);
            throw new RuntimeException("배치 처리 실패", e);
        }
    }

    /**
     * 배치용 특징 추출 프롬프트 생성
     */
    private String buildBatchExtractionPrompt(List<CrawlCoverLetter> batch) {
        StringBuilder prompt = new StringBuilder();
        
        prompt.append("""
            당신은 합격 자소서의 관찰 가능한 특징을 3축(표현력/구조/스토리)으로 추출하는 전문 분석가입니다.
            결과는 반드시 유효한 JSON 형식으로만 출력하고, 주어진 스키마를 정확히 준수하세요. 평가나 권고사항, 일반론은 절대 포함하지 마세요.
            
            [지침]
            - 각 자소서마다 각 카테고리에서 정확히 1개씩, 총 3개 특징 추출
            - 'feature_category'는 "EXPRESSION"(표현력), "STRUCTURE"(구조), "CONTENT"(이야기) 중 하나
            - 'description'은 특징을 한 문장으로 간결하게 설명 (100자 이내)
            - 'essay_id'는 해당 특징이 추출된 자소서의 ID
            - 글 내에서 실제로 관찰되는 패턴만 기술하고, 일반적인 조언은 금지
            
            [자소서 목록]
            """);
        
        for (int i = 0; i < batch.size(); i++) {
            CrawlCoverLetter essay = batch.get(i);
            prompt.append(String.format("""
                
                === 자소서 %d (ID: %d, 길이: %d자) ===
                %s
                """, i + 1, essay.getCoverLetterId(), essay.getText().length(), essay.getText()));
        }
        
        prompt.append("""
            
            [JSON 스키마]
            {
              "features": [
                {
                  "essay_id": 자소서_ID,
                  "feature_category": "EXPRESSION|STRUCTURE|CONTENT",
                  "description": "특징을 한 문장으로 설명"
                }
              ]
            }
            """);
        
        return prompt.toString();
    }

    /**
     * 배치 LLM 응답에서 특징 파싱
     */
    private List<FeatureCandidate> parseBatchFeatureResponse(String response, List<CrawlCoverLetter> batch) {
        try {
            // Gemini API 응답에서 텍스트 컨텐츠 추출
            String textContent = geminiResponseParser.extractTextContent(response);
            log.info("배치에서 텍스트 컨텐츠 추출 완료: {}자", textContent.length());
            
            // 추출된 텍스트를 JSON으로 파싱하여 특징 리스트 생성
            return parseBatchFeaturesFromText(textContent);
            
        } catch (Exception e) {
            log.error("배치 특징 파싱 실패", e);
            return new ArrayList<>();
        }
    }

    /**
     * 배치 텍스트에서 특징 리스트 파싱
     */
    private List<FeatureCandidate> parseBatchFeaturesFromText(String text) {
        try {
            // 코드 블록 제거 (```json ... ```)
            String cleanText = text.trim();
            if (cleanText.startsWith("```json")) {
                cleanText = cleanText.substring(7); // "```json" 제거
            }
            if (cleanText.startsWith("```")) {
                cleanText = cleanText.substring(3); // "```" 제거
            }
            if (cleanText.endsWith("```")) {
                cleanText = cleanText.substring(0, cleanText.length() - 3); // "```" 제거
            }
            cleanText = cleanText.trim();
            
            // text가 JSON 형식인지 확인
            if (!cleanText.startsWith("{")) {
                log.warn("JSON 형식이 아닌 응답: {}", cleanText.substring(0, Math.min(100, cleanText.length())));
                return new ArrayList<>();
            }

            var jsonNode = objectMapper.readTree(cleanText);
            
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
                    
                    candidates.add(new FeatureCandidate(featureCategory, description));
                }
            }

            log.info("배치에서 {}개의 특징 파싱 완료", candidates.size());
            return candidates;

        } catch (Exception e) {
            log.error("배치 특징 JSON 파싱 실패: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * 자소서 전체를 한 번에 LLM에 보내서 특징 추출
     */
    private List<FeatureCandidate> extractFeaturesFromFullEssay(CrawlCoverLetter essay) {
        try {
            log.info("자소서 전체를 LLM에 전송하여 특징 추출 시작");
            
            // 1. 자소서 전체 내용으로 프롬프트 생성
            String prompt = buildFullEssayExtractionPrompt(essay);
            
            // 2. LLM API 호출
            GeminiRequest request = new GeminiRequest(
                List.of(new GeminiRequest.Content(
                    List.of(new GeminiRequest.Content.Part(prompt))
                )),
                new GeminiRequest.GenerationConfig("0.7", "8192")
            );
            
            String response = coverLetterFeatureLlmFeignClient.analyzeRaw(request);
            
            // 3. 응답 파싱
            return parseFeatureResponse(response, essay);
            
        } catch (Exception e) {
            log.error("자소서 전체 특징 추출 실패", e);
            return new ArrayList<>();
        }
    }

    /**
     * 자소서 전체 내용으로 특징 추출 프롬프트 생성
     */
    private String buildFullEssayExtractionPrompt(CrawlCoverLetter essay) {
        return String.format("""
            당신은 합격 자소서의 관찰 가능한 특징을 3축(표현력/구조/스토리)으로 추출하는 전문 분석가입니다.
            결과는 반드시 유효한 JSON 형식으로만 출력하고, 주어진 스키마를 정확히 준수하세요. 평가나 권고사항, 일반론은 절대 포함하지 마세요.
            
            [메타] essay_id=%d, 전체_자소서_길이=%d자
            
            [지침]
            - 각 카테고리에서 정확히 1개씩, 총 3개 특징 추출
            - 'feature_category'는 "EXPRESSION"(표현력), "STRUCTURE"(구조), "CONTENT"(이야기) 중 하나
            - 'description'은 특징을 한 문장으로 간결하게 설명 (100자 이내)
            - 글 내에서 실제로 관찰되는 패턴만 기술하고, 일반적인 조언은 금지
            
            [자소서 전체 내용]
            %s
            
            [JSON 스키마]
            {
              "features": [
                {
                  "feature_category": "EXPRESSION|STRUCTURE|CONTENT",
                  "description": "특징을 한 문장으로 설명"
                }
              ]
            }
            """, essay.getCoverLetterId(), essay.getText().length(), essay.getText());
    }

    /**
     * LLM 응답에서 특징 파싱 (자소서 전체용)
     */
    private List<FeatureCandidate> parseFeatureResponse(String response, CrawlCoverLetter essay) {
        try {
            // Gemini API 응답에서 텍스트 컨텐츠 추출
            String textContent = geminiResponseParser.extractTextContent(response);
            log.info("자소서 {}에서 텍스트 컨텐츠 추출 완료: {}자", essay.getCoverLetterId(), textContent.length());
            
            // 추출된 텍스트를 JSON으로 파싱하여 특징 리스트 생성
            return parseFeaturesFromText(textContent);
            
        } catch (Exception e) {
            log.error("자소서 {}에서 특징 파싱 실패", essay.getCoverLetterId(), e);
            return new ArrayList<>();
        }
    }






    /**
     * 텍스트에서 특징 리스트 파싱
     */
    private List<FeatureCandidate> parseFeaturesFromText(String text) {
        try {
            // text가 JSON 형식인지 확인
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
                    
                    candidates.add(new FeatureCandidate(featureCategory, description));
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
     * 특징 중복 제거 및 병합
     */
    private List<FeatureCandidate> deduplicateFeatures(List<FeatureCandidate> candidates) {
        try {
            log.info("중복 제거 시작: {}개 후보", candidates.size());
            
            // featureCategory별로 그룹화
            Map<String, List<FeatureCandidate>> groupedByCategory = candidates.stream()
                .collect(Collectors.groupingBy(FeatureCandidate::featureCategory));
            
            List<FeatureCandidate> deduplicated = new ArrayList<>();
            
            // 각 featureCategory별로 중복 제거
            for (Map.Entry<String, List<FeatureCandidate>> entry : groupedByCategory.entrySet()) {
                String category = entry.getKey();
                List<FeatureCandidate> categoryCandidates = entry.getValue();
                
                log.info("{} 카테고리: {}개 후보 처리 중", category, categoryCandidates.size());
                
                // 간단한 중복 제거: description이 유사한 것들 제거
                List<FeatureCandidate> uniqueCategoryCandidates = removeSimilarDescriptions(categoryCandidates);
                
                log.info("{} 카테고리: 중복 제거 후 {}개", category, uniqueCategoryCandidates.size());
                deduplicated.addAll(uniqueCategoryCandidates);
            }
            
            log.info("중복 제거 완료: {}개 → {}개", candidates.size(), deduplicated.size());
            return deduplicated;
            
        } catch (Exception e) {
            log.error("중복 제거 중 오류 발생", e);
            return candidates; // 오류 시 원본 반환
        }
    }

    /**
     * 유사한 description을 가진 특징들 제거
     */
    private List<FeatureCandidate> removeSimilarDescriptions(List<FeatureCandidate> candidates) {
        List<FeatureCandidate> unique = new ArrayList<>();
        
        for (FeatureCandidate candidate : candidates) {
            boolean isDuplicate = false;
            
            for (FeatureCandidate existing : unique) {
                // 간단한 유사도 체크: description이 70% 이상 유사하면 중복으로 간주
                if (calculateSimilarity(candidate.description(), existing.description()) > 0.7) {
                    isDuplicate = true;
                    break;
                }
            }
            
            if (!isDuplicate) {
                unique.add(candidate);
            }
        }
        
        return unique;
    }

    /**
     * 두 문자열 간의 유사도 계산 (간단한 방식)
     */
    private double calculateSimilarity(String str1, String str2) {
        if (str1 == null || str2 == null) return 0.0;
        
        String s1 = str1.toLowerCase().trim();
        String s2 = str2.toLowerCase().trim();
        
        if (s1.equals(s2)) return 1.0;
        
        // 공통 단어 수 기반 유사도 계산
        String[] words1 = s1.split("\\s+");
        String[] words2 = s2.split("\\s+");
        
        Set<String> set1 = new HashSet<>(Arrays.asList(words1));
        Set<String> set2 = new HashSet<>(Arrays.asList(words2));
        
        Set<String> intersection = new HashSet<>(set1);
        intersection.retainAll(set2);
        
        Set<String> union = new HashSet<>(set1);
        union.addAll(set2);
        
        return union.isEmpty() ? 0.0 : (double) intersection.size() / union.size();
    }

    /**
     * 최종 100개 특징 선정 (표현력 34개, 구조 33개, 스토리 33개)
     */
    private List<FeatureCandidate> selectTop100Features(List<FeatureCandidate> features) {
        try {
            log.info("최종 100개 특징 선정 시작: {}개 후보", features.size());
            
            // featureCategory별로 그룹화
            Map<String, List<FeatureCandidate>> groupedByCategory = features.stream()
                .collect(Collectors.groupingBy(FeatureCandidate::featureCategory));
            
            List<FeatureCandidate> finalFeatures = new ArrayList<>();
            
            // 표현력 (EXPRESSION): 34개
            List<FeatureCandidate> expressionFeatures = groupedByCategory.getOrDefault("EXPRESSION", new ArrayList<>());
            int expressionCount = Math.min(34, expressionFeatures.size());
            finalFeatures.addAll(expressionFeatures.subList(0, expressionCount));
            log.info("표현력 특징: {}개 선정", expressionCount);
            
            // 구조 (STRUCTURE): 33개
            List<FeatureCandidate> structureFeatures = groupedByCategory.getOrDefault("STRUCTURE", new ArrayList<>());
            int structureCount = Math.min(33, structureFeatures.size());
            finalFeatures.addAll(structureFeatures.subList(0, structureCount));
            log.info("구조 특징: {}개 선정", structureCount);
            
            // 스토리 (CONTENT): 33개
            List<FeatureCandidate> storyFeatures = groupedByCategory.getOrDefault("CONTENT", new ArrayList<>());
            int storyCount = Math.min(33, storyFeatures.size());
            finalFeatures.addAll(storyFeatures.subList(0, storyCount));
            log.info("스토리 특징: {}개 선정", storyCount);
            
            // 부족한 경우 다른 카테고리에서 보충
            int totalSelected = finalFeatures.size();
            if (totalSelected < 100) {
                log.info("목표 100개에 {}개 부족, 다른 카테고리에서 보충", 100 - totalSelected);
                
                // 모든 특징을 하나의 리스트로 합치고 정렬
                List<FeatureCandidate> remainingFeatures = new ArrayList<>();
                for (List<FeatureCandidate> categoryFeatures : groupedByCategory.values()) {
                    remainingFeatures.addAll(categoryFeatures);
                }
                
                // 이미 선택된 것들 제외
                remainingFeatures.removeAll(finalFeatures);
                
                // 부족한 만큼 추가
                int additionalNeeded = 100 - totalSelected;
                int additionalCount = Math.min(additionalNeeded, remainingFeatures.size());
                finalFeatures.addAll(remainingFeatures.subList(0, additionalCount));
                
                log.info("추가 선정: {}개", additionalCount);
            }
            
            log.info("최종 특징 선정 완료: {}개", finalFeatures.size());
            return finalFeatures;
            
        } catch (Exception e) {
            log.error("최종 특징 선정 중 오류 발생", e);
            // 오류 시 단순히 100개 제한
            return features.stream().limit(100).toList();
        }
    }

    /**
     * LLM 응답의 featureCategory를 FeaturesCategory로 변환
     */
    private FeaturesCategory convertToCategory(String featureCategory) {
        try {
            return FeaturesCategory.valueOf(featureCategory.toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("알 수 없는 featureCategory: {}, 기본값 EXPRESSION 사용", featureCategory);
            return FeaturesCategory.EXPRESSION;
        }
    }

    /**
     * FeatureCandidate를 CoverLetterFeature 엔티티로 변환
     */
    private CoverLetterFeature convertToEntity(FeatureCandidate candidate) {
        FeaturesCategory category = convertToCategory(candidate.featureCategory());
        return new CoverLetterFeature(category, candidate.description());
    }

    /**
     * 특징들을 DB에 저장
     */
    private void saveFeaturesToDatabase(List<FeatureCandidate> features) {
        try {
            // 기존 특징 데이터 삭제 (새로운 분석 결과로 교체)
            featureRepository.deleteAll();
            log.info("기존 특징 데이터 삭제 완료");
            
            // 새로운 특징들을 엔티티로 변환하여 저장
            List<CoverLetterFeature> entities = features.stream()
                .map(this::convertToEntity)
                .toList();
            
            featureRepository.saveAll(entities);
            log.info("{}개의 새로운 특징을 DB에 저장 완료", entities.size());
            
        } catch (Exception e) {
            log.error("특징 DB 저장 중 오류 발생", e);
            throw new RuntimeException("특징 DB 저장 실패", e);
        }
    }
}
