package com.cvmento.global.usage.dto;

import lombok.Builder;

import java.time.LocalDateTime;

/**
 * 통합 토큰 사용량 정보 DTO
 */
@Builder
public record TokenUsageInfo(
        int remainingTokens,      // 남은 토큰 수
        int maxTokens,           // 최대 토큰 수
        LocalDateTime nextRefillTime, // 다음 충전 시간
        int refillAmount         // 충전 시 증가량
) {

    public static TokenUsageInfo empty() {
        return TokenUsageInfo.builder()
                .remainingTokens(0)
                .maxTokens(0)
                .build();
    }
}