package com.cvmento.global.usage.enums;

import lombok.Getter;
import java.time.LocalDateTime;

/**
 * 사용량 타입 정의 (통합 토큰 시스템 - 고정 충전 시점)
 */
@Getter
public enum UsageType {
    COVERLETTER_REVIEW("AI 자소서 첨삭", 5),
    INTERVIEW_AUTO("모의 면접 자동질문생성", 3),
    INTERVIEW_CUSTOM("커스텀 프롬프트 면접 답변생성", 1);

    private final String description;
    private final int cost;

    UsageType(String description, int cost) {
        this.description = description;
        this.cost = cost;
    }

    public static final int MAX_TOKENS = 40;                    // 최대 40개
    public static final int REFILL_INTERVAL_HOURS = 2;          // 2시간마다
    public static final int REFILL_AMOUNT = 10;                  // 10개씩 충전

    /**
     * 사용자별 토큰 Redis 키 생성.
     */
    public static String getTokenKey(Long userId) {
        return String.format("user:%d:tokens", userId);
    }

    /**
     * 전역 마지막 충전 시간 Redis 키.
     */
    public static String getGlobalLastRefillKey() {
        return "global:last_refill_time";
    }

    /**
     * 다음 고정 충전 시간 계산.
     * 매일 00:00, 02:00, 04:00, 06:00... 시점에 충전
     */
    public static LocalDateTime getNextRefillTime() {
        LocalDateTime now = LocalDateTime.now();

        int currentHour = now.getHour();
        int nextRefillHour = ((currentHour / REFILL_INTERVAL_HOURS) + 1) * REFILL_INTERVAL_HOURS;

        LocalDateTime nextRefill = now.withMinute(0).withSecond(0).withNano(0);

        if (nextRefillHour >= 24) {
            nextRefill = nextRefill.plusDays(1).withHour(0);
        } else {
            nextRefill = nextRefill.withHour(nextRefillHour);
        }

        return nextRefill;
    }
}
