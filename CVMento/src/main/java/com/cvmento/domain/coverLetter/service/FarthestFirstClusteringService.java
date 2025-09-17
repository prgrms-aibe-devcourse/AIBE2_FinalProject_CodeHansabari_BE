package com.cvmento.domain.coverLetter.service;

import com.cvmento.domain.coverLetter.entity.CoverLetterFeature;
import com.cvmento.domain.coverLetter.entity.RawCoverLetterFeature;
import com.cvmento.domain.coverLetter.enums.FeaturesCategory;
import com.cvmento.domain.coverLetter.repository.CoverLetterFeatureRepository;
import com.cvmento.domain.coverLetter.repository.RawCoverLetterFeatureRepository;
import com.cvmento.global.exception.customException.FeatureExtractionException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ai.djl.translate.TranslateException;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Farthest-First 클러스터링 기반 특징 중복제거 서비스
 * - k-center 문제의 farthest-first(Gonzalez) 탐욕 알고리즘 사용
 * - 서로 가장 멀리 떨어진 대표 k개를 선택 후 나머지를 할당
 * - 정확한 클러스터 수 보장 및 의미 공간의 균등한 커버리지
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class FarthestFirstClusteringService {

    private static final int EXPRESSION_CLUSTER_COUNT = 34;
    private static final int STRUCTURE_CLUSTER_COUNT = 33;
    private static final int CONTENT_CLUSTER_COUNT = 33;
    private static final double INITIAL_DEDUPLICATION_THRESHOLD = 0.98;

    private final RawCoverLetterFeatureRepository rawFeatureRepository;
    private final CoverLetterFeatureRepository featureRepository;
    private final EmbeddingSimilarityService embeddingService;

    /**
     * 카테고리별 farthest-first 클러스터링으로 중복제거
     */
    @Transactional
    public List<CoverLetterFeature> deduplicateFeaturesWithFarthestFirst() {
        try {
            log.info("Farthest-First 클러스터링 중복제거 시작");

            // 1. 모든 raw 특징 조회
            List<RawCoverLetterFeature> allRawFeatures = rawFeatureRepository.findAll();
            log.info("총 {}개의 raw 특징 조회", allRawFeatures.size());

            if (allRawFeatures.isEmpty()) {
                log.warn("raw_features에 데이터가 없습니다.");
                return new ArrayList<>();
            }

            // 2. 기존 cover_letter_features 데이터 삭제
            featureRepository.deleteAll();
            log.info("기존 cover_letter_features 데이터 삭제 완료");

            // 3. 카테고리별 farthest-first 클러스터링 수행
            List<CoverLetterFeature> allFinalFeatures = new ArrayList<>();
            
            // EXPRESSION: 34개 클러스터
            List<RawCoverLetterFeature> expressionFeatures = filterFeaturesByCategory(allRawFeatures, FeaturesCategory.EXPRESSION);
            if (!expressionFeatures.isEmpty()) {
                log.info("EXPRESSION 카테고리: {}개 특징 → {}개 클러스터", expressionFeatures.size(), EXPRESSION_CLUSTER_COUNT);
                List<CoverLetterFeature> expressionFinal = performFarthestFirstClustering(expressionFeatures, EXPRESSION_CLUSTER_COUNT);
                allFinalFeatures.addAll(expressionFinal);
                log.info("EXPRESSION 카테고리: {}개 최종 특징 선정", expressionFinal.size());
            }

            // STRUCTURE: 33개 클러스터
            List<RawCoverLetterFeature> structureFeatures = filterFeaturesByCategory(allRawFeatures, FeaturesCategory.STRUCTURE);
            if (!structureFeatures.isEmpty()) {
                log.info("STRUCTURE 카테고리: {}개 특징 → {}개 클러스터", structureFeatures.size(), STRUCTURE_CLUSTER_COUNT);
                List<CoverLetterFeature> structureFinal = performFarthestFirstClustering(structureFeatures, STRUCTURE_CLUSTER_COUNT);
                allFinalFeatures.addAll(structureFinal);
                log.info("STRUCTURE 카테고리: {}개 최종 특징 선정", structureFinal.size());
            }

            // CONTENT: 33개 클러스터
            List<RawCoverLetterFeature> contentFeatures = filterFeaturesByCategory(allRawFeatures, FeaturesCategory.CONTENT);
            if (!contentFeatures.isEmpty()) {
                log.info("CONTENT 카테고리: {}개 특징 → {}개 클러스터", contentFeatures.size(), CONTENT_CLUSTER_COUNT);
                List<CoverLetterFeature> contentFinal = performFarthestFirstClustering(contentFeatures, CONTENT_CLUSTER_COUNT);
                allFinalFeatures.addAll(contentFinal);
                log.info("CONTENT 카테고리: {}개 최종 특징 선정", contentFinal.size());
            }

            // 4. cover_letter_features 테이블에 저장
            featureRepository.saveAll(allFinalFeatures);
            log.info("총 {}개의 최종 특징을 cover_letter_features 테이블에 저장 완료", allFinalFeatures.size());

            return allFinalFeatures;

        } catch (Exception e) {
            log.error("Farthest-First 클러스터링 중 오류 발생", e);
            throw new FeatureExtractionException("Farthest-First 클러스터링 실패", e);
        }
    }

    private List<RawCoverLetterFeature> filterFeaturesByCategory(List<RawCoverLetterFeature> allFeatures, FeaturesCategory category) {
        return allFeatures.stream()
                .filter(feature -> feature.getFeaturesCategory() == category)
                .collect(Collectors.toList());
    }

    /**
     * Farthest-First 클러스터링 수행
     */
    private List<CoverLetterFeature> performFarthestFirstClustering(List<RawCoverLetterFeature> features, int k) {
        try {
            log.info("Farthest-First 클러스터링 시작: {}개 특징 → {}개 클러스터", features.size(), k);

            // 1. 초기 중복 제거 및 임베딩 생성
            List<String> descriptions = features.stream().map(RawCoverLetterFeature::getDescription).collect(Collectors.toList());
            DuplicateRemovalResult duplicateRemovalResult = removeNearDuplicates(descriptions, INITIAL_DEDUPLICATION_THRESHOLD);
            List<float[]> embeddings = generateEmbeddingsForFiltered(descriptions, duplicateRemovalResult.getFilteredIndices());

            // 2. 대표 선정 및 클러스터 할당
            List<Integer> representatives = selectFarthestFirstRepresentatives(embeddings, k);
            Map<Integer, List<Integer>> clusters = assignToClusters(embeddings, representatives);

            // 3. 메도이드 보정
            representatives = updateMedoids(embeddings, clusters, representatives);
            clusters = assignToClusters(embeddings, representatives);
            log.info("메도이드 보정 완료");

            // 4. 최종 특징 생성
            return createFinalFeatures(features, duplicateRemovalResult, representatives, clusters);

        } catch (Exception e) {
            log.error("Farthest-First 클러스터링 실패", e);
            return new ArrayList<>();
        }
    }

    private List<float[]> generateEmbeddingsForFiltered(List<String> allDescriptions, List<Integer> filteredIndices) throws TranslateException {
        List<String> filteredDescriptions = filteredIndices.stream()
                .map(allDescriptions::get)
                .collect(Collectors.toList());
        return embeddingService.generateEmbeddings(filteredDescriptions);
    }

    private List<CoverLetterFeature> createFinalFeatures(
            List<RawCoverLetterFeature> originalFeatures,
            DuplicateRemovalResult duplicateRemovalResult,
            List<Integer> representatives,
            Map<Integer, List<Integer>> clusters) {

        List<Integer> filteredIndices = duplicateRemovalResult.getFilteredIndices();
        Map<Integer, List<Integer>> originalToFiltered = duplicateRemovalResult.getOriginalToFiltered();

        List<CoverLetterFeature> finalFeatures = new ArrayList<>();
        for (int i = 0; i < representatives.size(); i++) {
            int representativeIndexInFiltered = representatives.get(i);
            int originalIndex = filteredIndices.get(representativeIndexInFiltered);
            RawCoverLetterFeature representativeFeature = originalFeatures.get(originalIndex);

            List<Integer> clusterMembers = clusters.get(i);
            int duplicateCount = calculateOriginalDuplicateCount(clusterMembers, originalToFiltered);

            CoverLetterFeature finalFeature = new CoverLetterFeature(
                    representativeFeature.getFeaturesCategory(),
                    representativeFeature.getDescription(),
                    duplicateCount,
                    representativeFeature.getCoverLetterId()
            );
            finalFeatures.add(finalFeature);
        }

        // 중복횟수 기준 내림차순 정렬
        finalFeatures.sort(Comparator.comparingInt(CoverLetterFeature::getDuplicateCount).reversed());
        log.info("Farthest-First 클러스터링 완료: {}개 최종 특징", finalFeatures.size());
        return finalFeatures;
    }

    private int calculateOriginalDuplicateCount(List<Integer> clusterMembers, Map<Integer, List<Integer>> originalToFilteredMap) {
        int count = 0;
        for (int memberIndexInFiltered : clusterMembers) {
            List<Integer> originalIndices = originalToFilteredMap.get(memberIndexInFiltered);
            if (originalIndices != null) {
                count += originalIndices.size();
            }
        }
        return count;
    }

    /**
     * 초기 중복 제거 (완전 중복 제거) - 원본 매핑 정보 포함
     */
    private DuplicateRemovalResult removeNearDuplicates(List<String> descriptions, double threshold) {
        try {
            List<Integer> filteredIndices = new ArrayList<>();
            Map<Integer, List<Integer>> originalToFiltered = new HashMap<>();
            boolean[] visited = new boolean[descriptions.size()];

            for (int i = 0; i < descriptions.size(); i++) {
                if (visited[i]) continue;

                // 현재 문장을 대표로 선택
                int filteredIndex = filteredIndices.size();
                filteredIndices.add(i);
                visited[i] = true;

                // 현재 문장과 중복되는 원본 인덱스들 수집
                List<Integer> originalIndices = new ArrayList<>();
                originalIndices.add(i);

                // 현재 문장과 유사한 문장들 마킹
                for (int j = i + 1; j < descriptions.size(); j++) {
                    if (visited[j]) continue;

                    double similarity = embeddingService.calculateSimilarity(
                        descriptions.get(i), descriptions.get(j));
                    
                    if (similarity >= threshold) {
                        visited[j] = true;
                        originalIndices.add(j); // 중복된 원본 인덱스 추가
                    }
                }

                // 필터링된 인덱스와 원본 인덱스들 매핑
                originalToFiltered.put(filteredIndex, originalIndices);
            }

            return new DuplicateRemovalResult(filteredIndices, originalToFiltered);
        } catch (Exception e) {
            log.error("초기 중복 제거 실패", e);
            // 실패 시 모든 인덱스를 개별적으로 매핑
            List<Integer> allIndices = new ArrayList<>();
            Map<Integer, List<Integer>> originalToFiltered = new HashMap<>();
            for (int i = 0; i < descriptions.size(); i++) {
                allIndices.add(i);
                originalToFiltered.put(i, Arrays.asList(i));
            }
            return new DuplicateRemovalResult(allIndices, originalToFiltered);
        }
    }

    /**
     * 중복 제거 결과를 담는 내부 클래스
     */
    private static class DuplicateRemovalResult {
        private final List<Integer> filteredIndices;
        private final Map<Integer, List<Integer>> originalToFiltered;

        public DuplicateRemovalResult(List<Integer> filteredIndices, Map<Integer, List<Integer>> originalToFiltered) {
            this.filteredIndices = filteredIndices;
            this.originalToFiltered = originalToFiltered;
        }

        public List<Integer> getFilteredIndices() {
            return filteredIndices;
        }

        public Map<Integer, List<Integer>> getOriginalToFiltered() {
            return originalToFiltered;
        }
    }

    /**
     * Farthest-First로 대표 k개 선택
     */
    private List<Integer> selectFarthestFirstRepresentatives(List<float[]> embeddings, int k) {
        try {
            int n = embeddings.size();
            if (n <= k) {
                // 특징 수가 k개 이하면 모든 특징을 대표로 선택
                log.warn("특징 수({})가 목표 클러스터 수({})보다 적음. 모든 특징을 대표로 선택", n, k);
                List<Integer> allIndices = new ArrayList<>();
                for (int i = 0; i < n; i++) {
                    allIndices.add(i);
                }
                return allIndices;
            }

            List<Integer> representatives = new ArrayList<>();
            double[] minDistances = new double[n];
            Arrays.fill(minDistances, Double.MAX_VALUE);

            // 1. 첫 번째 대표 선택: 가장 "중앙"인 특징 (평균 유사도 최대)
            int firstRepresentative = selectCentralRepresentative(embeddings);
            representatives.add(firstRepresentative);

            // 2. 나머지 k-1개 대표 선택: farthest-first
            for (int iter = 1; iter < k; iter++) {
                log.debug("Farthest-First 반복 {}: 현재 대표 {}개", iter, representatives.size());
                // 현재 대표들과의 최소 거리 업데이트
                for (int i = 0; i < n; i++) {
                    if (representatives.contains(i)) continue;

                    double minDist = Double.MAX_VALUE;
                    for (int rep : representatives) {
                        double dist = calculateCosineDistance(embeddings.get(i), embeddings.get(rep));
                        minDist = Math.min(minDist, dist);
                    }
                    minDistances[i] = minDist;
                }

                // 최소 거리가 가장 큰 특징 선택
                int farthestIndex = -1;
                double maxMinDist = -1;
                for (int i = 0; i < n; i++) {
                    if (representatives.contains(i)) continue;
                    if (minDistances[i] > maxMinDist) {
                        maxMinDist = minDistances[i];
                        farthestIndex = i;
                    }
                }

                if (farthestIndex != -1) {
                    representatives.add(farthestIndex);
                    log.debug("대표 {} 추가: 인덱스 {}", representatives.size(), farthestIndex);
                } else {
                    log.warn("Farthest-First 반복 {}에서 대표를 찾지 못함", iter);
                }
            }

            return representatives;
        } catch (Exception e) {
            log.error("Farthest-First 대표 선택 실패", e);
            return new ArrayList<>();
        }
    }

    /**
     * 가장 "중앙"인 특징 선택 (평균 유사도 최대)
     */
    private int selectCentralRepresentative(List<float[]> embeddings) {
        try {
            int n = embeddings.size();
            double maxAvgSimilarity = -1;
            int centralIndex = 0;

            for (int i = 0; i < n; i++) {
                double totalSimilarity = 0;
                for (int j = 0; j < n; j++) {
                    if (i != j) {
                        totalSimilarity += calculateCosineSimilarity(embeddings.get(i), embeddings.get(j));
                    }
                }
                double avgSimilarity = totalSimilarity / (n - 1);

                if (avgSimilarity > maxAvgSimilarity) {
                    maxAvgSimilarity = avgSimilarity;
                    centralIndex = i;
                }
            }

            return centralIndex;
        } catch (Exception e) {
            log.error("중앙 대표 선택 실패", e);
            return 0;
        }
    }

    /**
     * 나머지 특징들을 가장 가까운 대표에 할당
     */
    private Map<Integer, List<Integer>> assignToClusters(List<float[]> embeddings, List<Integer> representatives) {
        try {
            Map<Integer, List<Integer>> clusters = new HashMap<>();
            
            // 각 대표에 대한 클러스터 초기화
            for (int i = 0; i < representatives.size(); i++) {
                clusters.put(i, new ArrayList<>());
                clusters.get(i).add(representatives.get(i)); // 대표 자신도 포함
            }

            // 나머지 특징들을 가장 가까운 대표에 할당
            for (int i = 0; i < embeddings.size(); i++) {
                if (representatives.contains(i)) continue;

                double minDistance = Double.MAX_VALUE;
                int closestRepresentative = -1;

                for (int j = 0; j < representatives.size(); j++) {
                    int repIndex = representatives.get(j);
                    double distance = calculateCosineDistance(embeddings.get(i), embeddings.get(repIndex));
                    
                    if (distance < minDistance) {
                        minDistance = distance;
                        closestRepresentative = j;
                    }
                }

                if (closestRepresentative != -1) {
                    clusters.get(closestRepresentative).add(i);
                }
            }

            return clusters;
        } catch (Exception e) {
            log.error("클러스터 할당 실패", e);
            return new HashMap<>();
        }
    }

    /**
     * 메도이드 보정 (각 클러스터에서 가장 중앙에 있는 특징을 대표로 교체)
     */
    private List<Integer> updateMedoids(List<float[]> embeddings, Map<Integer, List<Integer>> clusters, List<Integer> representatives) {
        try {
            List<Integer> newRepresentatives = new ArrayList<>();

            for (int i = 0; i < representatives.size(); i++) {
                List<Integer> cluster = clusters.get(i);
                if (cluster.isEmpty()) {
                    newRepresentatives.add(representatives.get(i));
                    continue;
                }

                // 클러스터 내에서 가장 중앙에 있는 특징 찾기
                int bestMedoid = cluster.get(0);
                double minTotalDistance = Double.MAX_VALUE;

                for (int candidate : cluster) {
                    double totalDistance = 0;
                    for (int other : cluster) {
                        if (candidate != other) {
                            totalDistance += calculateCosineDistance(embeddings.get(candidate), embeddings.get(other));
                        }
                    }

                    if (totalDistance < minTotalDistance) {
                        minTotalDistance = totalDistance;
                        bestMedoid = candidate;
                    }
                }

                newRepresentatives.add(bestMedoid);
            }

            return newRepresentatives;
        } catch (Exception e) {
            log.error("메도이드 보정 실패", e);
            return representatives;
        }
    }

    /**
     * 코사인 거리 계산
     */
    private double calculateCosineDistance(float[] embedding1, float[] embedding2) {
        return 1.0 - calculateCosineSimilarity(embedding1, embedding2);
    }

    /**
     * 코사인 유사도 계산
     */
    private double calculateCosineSimilarity(float[] embedding1, float[] embedding2) {
        try {
            double dotProduct = 0;
            double norm1 = 0;
            double norm2 = 0;

            for (int i = 0; i < embedding1.length; i++) {
                dotProduct += embedding1[i] * embedding2[i];
                norm1 += embedding1[i] * embedding1[i];
                norm2 += embedding2[i] * embedding2[i];
            }

            if (norm1 == 0 || norm2 == 0) {
                return 0;
            }

            return dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2));
        } catch (Exception e) {
            log.error("코사인 유사도 계산 실패", e);
            return 0;
        }
    }
}