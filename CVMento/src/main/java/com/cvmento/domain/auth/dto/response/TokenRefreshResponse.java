package com.cvmento.domain.auth.dto.response;

import lombok.Builder;
import lombok.Getter;

// 토큰 갱신 응답
@Getter
@Builder
public class TokenRefreshResponse {
    private final String message;
}
