package com.cvmento.domain.auth.dto.response;

/**
 * 토큰 갱신 응답 DTO
 *
 * @param message 응답 메시지
 */
public record TokenRefreshResponse(
        String message
) {}
