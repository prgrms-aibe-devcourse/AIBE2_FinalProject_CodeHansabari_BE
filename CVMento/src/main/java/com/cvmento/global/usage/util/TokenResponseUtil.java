package com.cvmento.global.usage.util;

import com.cvmento.global.usage.dto.TokenUsageInfo;
import com.cvmento.global.usage.service.UsageTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 응답에 토큰 사용량 정보를 포함시키는 유틸리티
 */
@Component
@RequiredArgsConstructor
public class TokenResponseUtil {

    private final UsageTokenService usageTokenService;

    // 추후 추가 구현 부분

    /**
     * 응답 데이터에 토큰 사용량 정보를 추가 (이메일 기반)
     */
    public Map<String, Object> addTokenUsage(String userEmail, Object responseData) {
        TokenUsageInfo tokenUsage = usageTokenService.getTokenUsage(userEmail);

        return Map.of(
                "data", responseData,
                "tokenUsage", tokenUsage
        );
    }

    /**
     * 토큰 사용량 정보만 반환 (이메일 기반)
     */
    public TokenUsageInfo getTokenUsage(String userEmail) {
        return usageTokenService.getTokenUsage(userEmail);
    }
}