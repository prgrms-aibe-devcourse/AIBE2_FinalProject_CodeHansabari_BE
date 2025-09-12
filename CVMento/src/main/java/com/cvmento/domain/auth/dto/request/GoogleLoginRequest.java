package com.cvmento.domain.auth.dto.request;

import jakarta.validation.constraints.NotBlank;

/**
 * 구글 OAuth2 로그인 요청 DTO
 *
 * @param code       구글 Authorization Code
 * @param state      CSRF 방지를 위한 상태 값
 * @param redirectUri 로그인 완료 후 리다이렉트 URI
 */
public record GoogleLoginRequest(
        @NotBlank(message = "Authorization code는 필수입니다.")
        String code,
        String state,
        String redirectUri
) {}
