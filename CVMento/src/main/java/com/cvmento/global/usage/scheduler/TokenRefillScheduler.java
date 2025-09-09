package com.cvmento.global.usage.scheduler;

import com.cvmento.global.usage.service.UsageTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 고정 시점 토큰 충전 스케줄러
 * 매 2시간마다 (00:00, 02:00, 04:00...) 모든 사용자에게 토큰 충전
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class TokenRefillScheduler {

    private final UsageTokenService usageTokenService;

    /**
     * 매 시간 정각에 실행하여 충전 시점인지 확인
     * 2시간마다 (짝수 시간)일 때만 실제 충전 수행
     */
    @Scheduled(cron = "0 0 */2 * * *") // 매 2시간마다 (00:00, 02:00, 04:00, ...)
    public void refillAllUserTokens() {
        log.info("고정 시점 토큰 충전 시작");
        usageTokenService.refillAllUsersTokens();
    }
}