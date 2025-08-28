package com.cvmento.domain.auth.dto;

import java.time.LocalDateTime;

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
