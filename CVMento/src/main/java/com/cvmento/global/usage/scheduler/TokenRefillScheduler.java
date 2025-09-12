package com.cvmento.global.usage.scheduler;

import com.cvmento.global.usage.service.UsageTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 토큰 충전 스케줄러.
 * 매 2시간마다 모든 사용자에게 토큰 충전
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class TokenRefillScheduler {

    private final UsageTokenService usageTokenService;

    /**
     * 토큰 충전 실행.
     */
    @Scheduled(cron = "0 0 */2 * * *")
    public void refillAllUserTokens() {
        log.info("고정 시점 토큰 충전 시작");
        usageTokenService.refillAllUsersTokens();
    }
}