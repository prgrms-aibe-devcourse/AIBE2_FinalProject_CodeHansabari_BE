package com.cvmento.domain.auth.dto.response;

/**
 * 구글 OAuth2 로그인 URL 응답 DTO
 *
 * @param loginUrl 구글 로그인 URL
 * @param state    CSRF 방지를 위한 상태 값
 */
public record GoogleLoginUrlResponse(
        String loginUrl,
        String state
) {}
