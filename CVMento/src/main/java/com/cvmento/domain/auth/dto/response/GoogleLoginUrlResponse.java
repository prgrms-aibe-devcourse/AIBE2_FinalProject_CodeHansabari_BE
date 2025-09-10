package com.cvmento.domain.auth.dto.response;

/**
 * 구글 OAuth2 로그인 URL 생성 응답 DTO
 */
public record GoogleLoginUrlResponse(
        String loginUrl,
        String state
) {}