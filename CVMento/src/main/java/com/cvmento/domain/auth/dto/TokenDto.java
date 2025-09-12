package com.cvmento.domain.auth.dto;

import java.time.LocalDateTime;

/**
 * 인증 토큰 DTO
 *
 * @param accessToken           액세스 토큰
 * @param refreshToken          리프레시 토큰
 * @param accessTokenExpiresAt  액세스 토큰 만료 시간
 * @param refreshTokenExpiresAt 리프레시 토큰 만료 시간
 */
public record TokenDto(
        String accessToken,
        String refreshToken,
        LocalDateTime accessTokenExpiresAt,
        LocalDateTime refreshTokenExpiresAt
) {
    public static TokenDto of(String accessToken, String refreshToken,
                              LocalDateTime accessTokenExpiresAt, LocalDateTime refreshTokenExpiresAt) {
        return new TokenDto(accessToken, refreshToken, accessTokenExpiresAt, refreshTokenExpiresAt);
    }
}
