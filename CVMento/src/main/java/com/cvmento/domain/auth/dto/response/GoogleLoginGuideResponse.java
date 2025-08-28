package com.cvmento.domain.auth.dto.response;

import lombok.Builder;
import lombok.Getter;

// 구글 로그인 안내 응답
@Getter
@Builder
public class GoogleLoginGuideResponse {
    private final String message;
    private final String loginUrl;
    private final String fullUrl;
    private final String note;
}
