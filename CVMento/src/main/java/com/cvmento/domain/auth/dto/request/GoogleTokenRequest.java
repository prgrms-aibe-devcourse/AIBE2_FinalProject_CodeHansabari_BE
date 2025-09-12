package com.cvmento.domain.auth.dto.request;

import jakarta.validation.constraints.NotBlank;

/**
 * 구글 ID 토큰 로그인 요청 DTO
 *
 * @param idToken 구글에서 발급받은 ID 토큰
 */
public record GoogleTokenRequest(
        @NotBlank(message = "Google ID Token은 필수입니다.")
        String idToken
) {}
