package com.cvmento.global.exception.customException;


import com.cvmento.global.usage.enums.UsageType;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 통합 토큰 사용량 제한 초과 예외
 */
@Getter
public class UsageLimitExceededException extends RuntimeException {

    private final UsageType usageType;
    private final int remainingTokens;
    private final int requiredTokens;
    private final LocalDateTime nextRefillTime;

    public UsageLimitExceededException(UsageType usageType, int remainingTokens, int requiredTokens, LocalDateTime nextRefillTime) {
        super(buildMessage(usageType, remainingTokens, requiredTokens, nextRefillTime));
        this.usageType = usageType;
        this.remainingTokens = remainingTokens;
        this.requiredTokens = requiredTokens;
        this.nextRefillTime = nextRefillTime;
    }

    private static String buildMessage(UsageType usageType, int remainingTokens, int requiredTokens, LocalDateTime nextRefillTime) {
        return String.format("토큰이 부족합니다. 기능: %s, 필요: %d개, 보유: %d개, 다음 충전: %s",
                usageType.getDescription(), requiredTokens, remainingTokens, nextRefillTime);
    }
}