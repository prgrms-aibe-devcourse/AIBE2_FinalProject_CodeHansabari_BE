package com.cvmento.domain.coverLetter.service;

import ai.djl.Application;
import ai.djl.huggingface.translator.TextEmbeddingTranslatorFactory;
import ai.djl.inference.Predictor;
import ai.djl.ndarray.NDArray;
import ai.djl.ndarray.NDManager;
import ai.djl.ndarray.types.Shape;
import ai.djl.repository.zoo.Criteria;
import ai.djl.repository.zoo.ZooModel;
import ai.djl.translate.TranslateException;
import ai.djl.training.util.ProgressBar;
import com.cvmento.global.exception.customException.FeatureExtractionException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.util.*;

/**
 * DJL과 허깅페이스를 사용한 임베딩 기반 문장 유사도 서비스
 * - sentence-transformers/paraphrase-multilingual-MiniLM-L12-v2 모델 사용
 * - 배치 처리로 성능 최적화
 * - 코사인 유사도 기반 클러스터링
 */
// @Service  // 임시로 비활성화 (다른 서버로 분리 예정)
@Slf4j
public class EmbeddingSimilarityService {

    @Value("${embedding.model.url:djl://ai.djl.huggingface.pytorch/sentence-transformers/paraphrase-multilingual-MiniLM-L12-v2}")
    private String modelUrl;

    @Value("${embedding.model.threshold:0.75}")
    private double similarityThreshold;

    @Value("${embedding.cache.dir:${user.home}/.djl-cache}")
    private String cacheDir;

    private ZooModel<String, float[]> model;
    private Predictor<String, float[]> predictor;

    @PostConstruct
    public void initializeModel() {
        try {
            log.info("임베딩 모델 초기화 시작: {}", modelUrl);
            
            // DJL 캐시 디렉토리 설정
            System.setProperty("DJL_CACHE_DIR", cacheDir);
            System.setProperty("ai.djl.logging.level", "info");

            Criteria<String, float[]> criteria = Criteria.builder()
                    .optApplication(Application.NLP.TEXT_EMBEDDING)
                    .setTypes(String.class, float[].class)
                    .optModelUrls(modelUrl)
                    .optEngine("PyTorch")
                    .optTranslatorFactory(new TextEmbeddingTranslatorFactory())
                    .optProgress(new ProgressBar())
                    .build();

            model = criteria.loadModel();
            predictor = model.newPredictor();
            
            log.info("임베딩 모델 초기화 완료");

        } catch (Exception e) {
            log.error("임베딩 모델 초기화 실패", e);
            throw new FeatureExtractionException("임베딩 모델 초기화 실패", e);
        }
    }

    @PreDestroy
    public void cleanup() {
        try {
            if (predictor != null) {
                predictor.close();
            }
            if (model != null) {
                model.close();
            }
            log.info("임베딩 모델 리소스 정리 완료");
        } catch (Exception e) {
            log.error("임베딩 모델 리소스 정리 중 오류", e);
        }
    }

    /**
     * 문장 리스트에 대해 유사도 기반 클러스터링 수행
     * @param sentences 클러스터링할 문장 리스트
     * @return 클러스터 리스트 (각 클러스터는 유사한 문장들의 인덱스 리스트)
     */
    public List<List<Integer>> findSimilarClusters(List<String> sentences) {
        try {
            log.info("유사도 기반 클러스터링 시작: {}개 문장", sentences.size());

            if (sentences.isEmpty()) {
                return new ArrayList<>();
            }

            // 1. 배치 임베딩 생성
            List<float[]> embeddings = generateEmbeddings(sentences);
            
            // 2. 코사인 유사도 행렬 계산
            float[][] similarityMatrix = calculateCosineSimilarityMatrix(embeddings);
            
            // 3. 연결요소 분석으로 클러스터 생성
            List<List<Integer>> clusters = findConnectedComponents(similarityMatrix, similarityThreshold);
            
            log.info("클러스터링 완료: {}개 클러스터 생성", clusters.size());
            return clusters;

        } catch (Exception e) {
            log.error("유사도 기반 클러스터링 실패", e);
            throw new FeatureExtractionException("클러스터링 실패", e);
        }
    }

    /**
     * 문장 리스트에 대한 임베딩 생성 (public 메서드로 변경)
     */
    public List<float[]> generateEmbeddings(List<String> sentences) throws TranslateException {
        try {
            // 배치 추론 시도
            return predictor.batchPredict(sentences);
        } catch (Exception e) {
            log.warn("배치 추론 실패, 개별 추론으로 폴백", e);
            // 개별 추론으로 폴백
            List<float[]> embeddings = new ArrayList<>();
            for (String sentence : sentences) {
                embeddings.add(predictor.predict(sentence));
            }
            return embeddings;
        }
    }

    /**
     * 임베딩 리스트로부터 코사인 유사도 행렬 계산 (public 메서드로 변경)
     */
    public float[][] calculateCosineSimilarityMatrix(List<float[]> embeddings) {
        try (NDManager manager = NDManager.newBaseManager()) {
            int n = embeddings.size();
            int d = embeddings.get(0).length;

            // 1. List<float[]> -> NDArray [N, D] 변환
            float[] flat = new float[n * d];
            for (int i = 0; i < n; i++) {
                System.arraycopy(embeddings.get(i), 0, flat, i * d, d);
            }
            NDArray E = manager.create(flat, new Shape(n, d));

            // 2. L2 정규화
            NDArray norm = E.norm(new int[]{1}, true);
            NDArray En = E.div(norm.add(1e-12f));

            // 3. 코사인 유사도 행렬 계산
            NDArray S = En.dot(En.transpose());

            // 4. float[][]로 변환
            long rows = S.getShape().get(0);
            long cols = S.getShape().get(1);
            float[] simFlat = S.toFloatArray();
            float[][] similarityMatrix = new float[(int) rows][(int) cols];
            
            for (int i = 0; i < rows; i++) {
                System.arraycopy(simFlat, (int) (i * cols), similarityMatrix[i], 0, (int) cols);
            }

            return similarityMatrix;
        }
    }

    /**
     * 유사도 행렬을 기반으로 연결요소(클러스터) 찾기
     */
    private List<List<Integer>> findConnectedComponents(float[][] similarityMatrix, double threshold) {
        int n = similarityMatrix.length;
        boolean[] visited = new boolean[n];
        List<List<Integer>> clusters = new ArrayList<>();

        for (int i = 0; i < n; i++) {
            if (visited[i]) continue;

            // BFS로 연결요소 탐색
            Deque<Integer> queue = new ArrayDeque<>();
            List<Integer> cluster = new ArrayList<>();
            
            queue.add(i);
            visited[i] = true;

            while (!queue.isEmpty()) {
                int current = queue.poll();
                cluster.add(current);

                // 현재 노드와 유사도가 임계값 이상인 노드들 탐색
                for (int j = 0; j < n; j++) {
                    if (!visited[j] && current != j && similarityMatrix[current][j] >= threshold) {
                        visited[j] = true;
                        queue.add(j);
                    }
                }
            }

            clusters.add(cluster);
        }

        return clusters;
    }

    /**
     * 클러스터에서 대표 문장 선택 (메도이드 방식)
     * @param cluster 클러스터 인덱스 리스트
     * @param sentences 전체 문장 리스트
     * @param similarityMatrix 유사도 행렬
     * @return 대표 문장의 인덱스
     */
    public int selectRepresentative(List<Integer> cluster, List<String> sentences, float[][] similarityMatrix) {
        if (cluster.size() == 1) {
            return cluster.get(0);
        }

        double bestScore = -1;
        int bestIndex = cluster.get(0);

        for (int candidate : cluster) {
            double totalSimilarity = 0;
            for (int other : cluster) {
                if (candidate != other) {
                    totalSimilarity += similarityMatrix[candidate][other];
                }
            }
            
            if (totalSimilarity > bestScore) {
                bestScore = totalSimilarity;
                bestIndex = candidate;
            }
        }

        return bestIndex;
    }

    /**
     * 목표 클러스터 수에 맞는 최적 임계값 찾기
     */
    public double findOptimalThreshold(List<String> sentences, int targetClusters) {
        try {
            log.info("최적 임계값 찾기 시작: {}개 문장, 목표 {}개 클러스터", sentences.size(), targetClusters);
            
            // 1. 임베딩 생성
            List<float[]> embeddings = generateEmbeddings(sentences);
            
            // 2. 이진 탐색으로 최적 임계값 찾기 (범위 확장)
            double low = 0.3, high = 0.9;
            double bestThreshold = 0.75;
            int bestDiff = Integer.MAX_VALUE;
            
            for (int i = 0; i < 15; i++) {
                double mid = (low + high) / 2;
                float[][] similarityMatrix = calculateCosineSimilarityMatrix(embeddings);
                int clusters = findConnectedComponents(similarityMatrix, mid).size();
                
                int diff = Math.abs(clusters - targetClusters);
                if (diff < bestDiff) {
                    bestDiff = diff;
                    bestThreshold = mid;
                }
                
                log.debug("임계값: {:.3f}, 클러스터: {}개, 차이: {}", mid, clusters, diff);
                
                if (clusters == targetClusters) {
                    log.info("정확한 임계값 찾음: {:.3f}", mid);
                    return mid;
                } else if (clusters < targetClusters) {
                    high = mid; // 임계값 낮춰서 더 많은 클러스터
                } else {
                    low = mid;  // 임계값 높여서 더 적은 클러스터
                }
            }
            
            float[][] finalSimilarityMatrix = calculateCosineSimilarityMatrix(embeddings);
            int finalClusters = findConnectedComponents(finalSimilarityMatrix, bestThreshold).size();
            log.info("최적 임계값: {:.3f}, 클러스터: {}개, 차이: {}", bestThreshold, finalClusters, bestDiff);
            
            // 목표 클러스터 수에 도달하지 못한 경우 더 낮은 임계값 시도
            if (finalClusters < targetClusters && bestDiff > 5) {
                log.warn("목표 클러스터 수에 도달하지 못함. 더 낮은 임계값으로 재시도");
                double fallbackThreshold = Math.max(0.2, bestThreshold - 0.1);
                int fallbackClusters = findConnectedComponents(finalSimilarityMatrix, fallbackThreshold).size();
                log.info("폴백 임계값: {:.3f}, 클러스터: {}개", fallbackThreshold, fallbackClusters);
                return fallbackThreshold;
            }
            
            return bestThreshold;
            
        } catch (Exception e) {
            log.error("최적 임계값 찾기 실패", e);
            return 0.75; // 기본값
        }
    }

    /**
     * 지정된 임계값으로 클러스터링 수행
     */
    public List<List<Integer>> findSimilarClustersWithThreshold(List<String> sentences, double threshold) {
        try {
            log.info("지정된 임계값으로 클러스터링 시작: {}개 문장, 임계값: {:.3f}", sentences.size(), threshold);

            if (sentences.isEmpty()) {
                return new ArrayList<>();
            }

            // 1. 배치 임베딩 생성
            List<float[]> embeddings = generateEmbeddings(sentences);
            
            // 2. 코사인 유사도 행렬 계산
            float[][] similarityMatrix = calculateCosineSimilarityMatrix(embeddings);
            
            // 3. 지정된 임계값으로 클러스터 생성
            List<List<Integer>> clusters = findConnectedComponents(similarityMatrix, threshold);
            
            log.info("클러스터링 완료: {}개 클러스터 생성", clusters.size());
            return clusters;

        } catch (Exception e) {
            log.error("지정된 임계값으로 클러스터링 실패", e);
            throw new FeatureExtractionException("클러스터링 실패", e);
        }
    }

    /**
     * 두 문장 간의 코사인 유사도 계산
     */
    public double calculateSimilarity(String sentence1, String sentence2) {
        try {
            List<String> sentences = Arrays.asList(sentence1, sentence2);
            List<float[]> embeddings = generateEmbeddings(sentences);
            
            if (embeddings.size() != 2) {
                return 0.0;
            }

            float[][] similarityMatrix = calculateCosineSimilarityMatrix(embeddings);
            return similarityMatrix[0][1];

        } catch (Exception e) {
            log.error("문장 유사도 계산 실패", e);
            return 0.0;
        }
    }
}