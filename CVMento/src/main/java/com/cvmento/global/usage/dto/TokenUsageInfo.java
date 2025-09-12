package com.cvmento.global.usage.dto;

import java.time.LocalDateTime;

/**
 * 통합 토큰 사용량 정보.
 *
 * @param remainingTokens 남은 토큰 수
 * @param maxTokens 최대 토큰 수
 * @param nextRefillTime 다음 충전 시간
 * @param refillAmount 충전 시 증가량
 */
public record TokenUsageInfo(
        int remainingTokens,
        int maxTokens,
        LocalDateTime nextRefillTime,
        int refillAmount
) {
    /**
     * 빈 토큰 정보 생성.
     */
    public static TokenUsageInfo empty() {
        return new TokenUsageInfo(0, 0, null, 0);
    }
}