package com.cvmento.domain.auth.dto.response;

/**
 * Google OAuth2 토큰 응답 DTO
 *
 * @param accessToken Google 액세스 토큰
 * @param idToken     Google ID 토큰 (선택적)
 * @param expiresIn   토큰 만료 시간(초)
 */
public record GoogleTokenResponse(
        String accessToken,
        String idToken,
        int expiresIn
) {
}