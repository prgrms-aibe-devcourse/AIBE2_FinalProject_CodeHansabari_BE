package com.cvmento.domain.coverLetter.service;

import com.cvmento.domain.coverLetter.client.CoverLetterFeatureLlmFeignClient;
import com.cvmento.domain.coverLetter.dto.request.LlmRequest;
import com.cvmento.domain.coverLetter.dto.response.EssayChunk;
import com.cvmento.domain.coverLetter.dto.response.FeatureCandidate;
import com.cvmento.domain.coverLetter.entity.CrawlCoverLetter;
import com.cvmento.domain.coverLetter.entity.CoverLetterFeature;
import com.cvmento.domain.coverLetter.enums.FeaturesCategory;
import com.cvmento.domain.coverLetter.repository.CrawlCoverLetterRepository;
import com.cvmento.domain.coverLetter.repository.CoverLetterFeatureRepository;
import com.cvmento.global.common.util.OpenAiResponseParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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
    private final OpenAiResponseParser openAiResponseParser;
    private final ExecutorService executorService = Executors.newFixedThreadPool(4);

    @Value("${feature.extraction.chunk.overlap-ratio:0.15}")
    private double overlapRatio;

    @Value("${feature.extraction.chunk.min-chunk-size:600}")
    private int minChunkSize;

    @Value("${feature.extraction.chunk.max-chunk-size:1200}")
    private int maxChunkSize;

    /**
     * 크롤링된 자소서 데이터에서 특징을 추출하는 메인 메서드
     */
    public List<FeatureCandidate> extractFeaturesFromCrawledData() {
        try {
            log.info("크롤링된 자소서 데이터에서 특징 추출 시작");
            
            // 1. 크롤링된 자소서 데이터 조회
            List<CrawlCoverLetter> crawledEssays = crawlRepository.findAll();
            log.info("총 {}개의 크롤링된 자소서 발견", crawledEssays.size());
            
            // 2. 자소서를 청크로 분할
            List<EssayChunk> chunks = createChunks(crawledEssays);
            log.info("총 {}개의 청크 생성", chunks.size());
            
            // 3. 각 청크에서 특징 추출 (병렬 처리)
            List<FeatureCandidate> allCandidates = extractFeaturesFromChunks(chunks);
            log.info("총 {}개의 특징 후보 추출", allCandidates.size());
            
            // 4. 중복 제거 및 병합
            List<FeatureCandidate> deduplicatedFeatures = deduplicateFeatures(allCandidates);
            log.info("중복 제거 후 {}개의 특징", deduplicatedFeatures.size());
            
            // 5. 최종 100개 선정
            List<FeatureCandidate> finalFeatures = selectTop100Features(deduplicatedFeatures);
            log.info("최종 {}개의 특징 선정 완료", finalFeatures.size());
            
            // 6. DB에 저장
            saveFeaturesToDatabase(finalFeatures);
            log.info("{}개의 특징을 DB에 저장 완료", finalFeatures.size());
            
            return finalFeatures;
            
        } catch (Exception e) {
            log.error("특징 추출 중 오류 발생", e);
            throw new RuntimeException("특징 추출 실패", e);
        }
    }

    /**
     * 자소서를 비율 기반 오버랩으로 청킹
     * - 글자 수에 따라 2-4개 청크로 분할
     * - 각 청크 간 15% 오버랩으로 문맥 보존
     */
    private List<EssayChunk> createChunks(List<CrawlCoverLetter> essays) {
        List<EssayChunk> chunks = new ArrayList<>();
        
        for (CrawlCoverLetter essay : essays) {
            String content = essay.getText();
            if (content == null || content.trim().isEmpty()) continue;
            
            int totalLength = content.length();
            log.debug("자소서 {} 처리 중: 총 {}자", essay.getCoverLetterId(), totalLength);
            
            // 글자 수에 따라 청크 수 결정
            int chunkCount = determineChunkCount(totalLength);
            int chunkSize = totalLength / chunkCount;
            
            log.debug("자소서 {}: {}개 청크로 분할, 청크 크기: {}자", 
                     essay.getCoverLetterId(), chunkCount, chunkSize);
            
            // 각 청크 생성 (오버랩 포함)
            for (int i = 0; i < chunkCount; i++) {
                EssayChunk chunk = createChunkWithOverlap(
                    essay, content, i, chunkCount, chunkSize, totalLength
                );
                chunks.add(chunk);
            }
        }
        
        log.info("총 {}개 자소서에서 {}개 청크 생성 완료", essays.size(), chunks.size());
        return chunks;
    }

    /**
     * 글자 수에 따라 청크 수 결정
     */
    private int determineChunkCount(int totalLength) {
        if (totalLength <= minChunkSize * 2) {
            return 2;  // 짧은 자소서: 2개 청크
        } else if (totalLength <= maxChunkSize * 2) {
            return 3;  // 중간 자소서: 3개 청크
        } else {
            return 4;  // 긴 자소서: 4개 청크
        }
    }

    /**
     * 오버랩을 포함한 개별 청크 생성
     */
    private EssayChunk createChunkWithOverlap(
            CrawlCoverLetter essay, String content, int chunkIndex, 
            int chunkCount, int chunkSize, int totalLength) {
        
        // 오버랩 크기 계산
        int overlapSize = (int) (chunkSize * overlapRatio);
        
        // 청크 시작/끝 위치 계산
        int targetStart = chunkIndex * chunkSize;
        int targetEnd = (chunkIndex + 1) * chunkSize;
        
        // 오버랩 적용
        int actualStart = Math.max(0, targetStart - overlapSize);
        int actualEnd = Math.min(totalLength, targetEnd + overlapSize);
        
        // 첫 번째 청크는 앞쪽 오버랩 제외
        if (chunkIndex == 0) {
            actualStart = 0;
        }
        
        // 마지막 청크는 뒤쪽 오버랩 제외
        if (chunkIndex == chunkCount - 1) {
            actualEnd = totalLength;
        }
        
        // 청크 내용 추출
        String chunkContent = content.substring(actualStart, actualEnd);
        
        log.debug("청크 {} 생성: {}자 ({}~{}), 오버랩: {}자", 
                 chunkIndex, chunkContent.length(), actualStart, actualEnd, overlapSize);
        
        return new EssayChunk(
            essay.getCoverLetterId(),
            chunkIndex,
            chunkContent,
            actualStart,
            actualEnd
        );
    }

    /**
     * 청크들에서 특징 추출 (병렬 처리)
     */
    private List<FeatureCandidate> extractFeaturesFromChunks(List<EssayChunk> chunks) {
        List<CompletableFuture<List<FeatureCandidate>>> futures = new ArrayList<>();
        
        for (EssayChunk chunk : chunks) {
            CompletableFuture<List<FeatureCandidate>> future = CompletableFuture.supplyAsync(() -> {
                try {
                    return extractFeaturesFromChunk(chunk);
                } catch (Exception e) {
                    log.error("청크 {}에서 특징 추출 실패", chunk.chunkIndex(), e);
                    return new ArrayList<FeatureCandidate>();
                }
            }, executorService);
            
            futures.add(future);
        }
        
        // 모든 비동기 작업 완료 대기
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        
        // 결과 수집
        List<FeatureCandidate> allCandidates = new ArrayList<>();
        for (CompletableFuture<List<FeatureCandidate>> future : futures) {
            try {
                allCandidates.addAll(future.get());
            } catch (Exception e) {
                log.error("특징 추출 결과 수집 실패", e);
            }
        }
        
        return allCandidates;
    }

    /**
     * 단일 청크에서 특징 추출
     */
    private List<FeatureCandidate> extractFeaturesFromChunk(EssayChunk chunk) {
        try {
            // LLM 프롬프트 생성
            String prompt = buildExtractionPrompt(chunk);
            
            // LLM API 호출
            LlmRequest request = new LlmRequest(
                "gpt-5-nano",  // 팀원들과 동일한 모델 사용
                prompt
            );
            
            String response = coverLetterFeatureLlmFeignClient.analyzeRaw(request);
            
            // 응답 파싱
            return parseFeatureResponse(response, chunk);
            
        } catch (Exception e) {
            log.error("청크 {}에서 특징 추출 실패", chunk.chunkIndex(), e);
            return new ArrayList<>();
        }
    }

    /**
     * 특징 추출용 프롬프트 생성
     */
    private String buildExtractionPrompt(EssayChunk chunk) {
        return String.format("""
            당신은 합격 자소서의 관찰 가능한 특징을 3축(표현력/구조/스토리)으로 추출하는 분석가입니다.
            결과는 JSON만 출력하고, 스키마를 반드시 지키세요. 평가/권고/일반론 금지.
            
            [메타] essay_id=%d, chunk_index=%d, chunk_char_offset=%d
            
            [지침]
            - 각 카테고리에서 최대 5개, 총 최대 15개
            - 'feature_category'는 "EXPRESSION"(표현력), "STRUCTURE"(구조), "CONTENT"(이야기) 중 하나
            - 'description'은 특징을 한 문장으로 간결하게 설명 (100자 이내)
            - 글 내에서 실제로 관찰되는 패턴만 기술하고, 일반적인 조언은 금지
            
            [청크]
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
            """, chunk.essayId(), chunk.chunkIndex(), chunk.charStart(), chunk.content());
    }

    /**
     * LLM 응답에서 특징 파싱 (팀원들과 동일한 방식)
     */
    private List<FeatureCandidate> parseFeatureResponse(String response, EssayChunk chunk) {
        try {
            // OpenAI /responses API 응답에서 텍스트 컨텐츠 추출
            String textContent = openAiResponseParser.extractTextContent(response);
            log.info("청크 {}에서 텍스트 컨텐츠 추출 완료: {}자", chunk.chunkIndex(), textContent.length());
            
            // 추출된 텍스트를 JSON으로 파싱하여 특징 리스트 생성
            return parseFeaturesFromText(textContent);
            
        } catch (Exception e) {
            log.error("청크 {}에서 특징 파싱 실패", chunk.chunkIndex(), e);
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
