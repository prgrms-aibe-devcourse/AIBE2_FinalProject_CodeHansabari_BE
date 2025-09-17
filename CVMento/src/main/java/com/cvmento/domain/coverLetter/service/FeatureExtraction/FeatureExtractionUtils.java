package com.cvmento.domain.coverLetter.service.FeatureExtraction;

import com.cvmento.domain.coverLetter.constants.GeminiConstants;
import com.cvmento.domain.coverLetter.entity.CrawlCoverLetter;
import com.cvmento.domain.coverLetter.enums.FeaturesCategory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 특징 추출 관련 공통 유틸리티 클래스
 * - 사고 예산 선택
 * - 카테고리 변환
 * - 최적 배치 크기 계산
 * - 기타 공통 유틸리티 메서드들
 */
@Component
@Slf4j
public class FeatureExtractionUtils {

    /**
     * Gemini 2.5 Flash 사고 예산을 동적으로 선택
     * 상수는 GeminiConstants 클래스에서 관리
     */
    public String selectThinkingBudget(int batchSize, int coverLetterCount) {
        if (batchSize <= 2 && coverLetterCount <= 5) {
            return GeminiConstants.THINKING_BUDGET_LOW;
        } else if (batchSize <= 5 && coverLetterCount <= 10) {
            return GeminiConstants.THINKING_BUDGET_MEDIUM;
        } else {
            return GeminiConstants.THINKING_BUDGET_HIGH;
        }
    }

    /**
     * 카테고리 문자열을 enum으로 변환
     */
    public FeaturesCategory convertToCategory(String featureCategory) {
        try {
            return FeaturesCategory.valueOf(featureCategory.toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("알 수 없는 featureCategory: {}, 기본값 EXPRESSION 사용", featureCategory);
            return FeaturesCategory.EXPRESSION;
        }
    }

    /**
     * 토큰 제한 고려한 배치 크기 계산
     */
    public int calculateOptimalBatchSize(List<CrawlCoverLetter> coverLetters) {
        if (coverLetters.isEmpty()) return 1;

        double avgLength = coverLetters.stream()
                .mapToInt(coverLetter -> coverLetter.getText().length())
                .average()
                .orElse(1000.0);

        int maxTokensPerBatch = 6000; // 8192 - 안전 마진
        int tokensPerCoverLetter = (int) (avgLength * 1.5);

        int optimalBatchSize = Math.max(1, maxTokensPerBatch / tokensPerCoverLetter);
        optimalBatchSize = Math.min(10, Math.max(1, optimalBatchSize));

        log.info("최적 배치 크기 계산: 평균 자소서 길이 {}자, 배치 크기 {}개",
                (int) avgLength, optimalBatchSize);

        return optimalBatchSize;
    }

    /**
     * 요청 간 지연 추가
     */
    public void addDelayBetweenRequests() {
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("요청 간 지연이 중단되었습니다.");
        }
    }

    /**
     * 성공적인 응답에 따른 지연 시간 조정
     */
    public long adjustDelayForSuccess(long responseTime, long currentDelay, long baseDelay, long maxDelay) {
        if (responseTime < 2000) {
            // 응답이 빠르면 지연 시간 감소
            currentDelay = Math.max(currentDelay - 1000, baseDelay);
            log.debug("빠른 응답으로 지연 시간 감소: {}ms", currentDelay);
        } else if (responseTime > 10000) {
            // 응답이 느리면 지연 시간 증가
            currentDelay = Math.min(currentDelay + 2000, maxDelay);
            log.debug("느린 응답으로 지연 시간 증가: {}ms", currentDelay);
        }
        return currentDelay;
    }

    /**
     * 배치 크기 유효성 검증
     */
    public boolean isValidBatchSize(int batchSize) {
        return batchSize >= 1 && batchSize <= 10;
    }
}