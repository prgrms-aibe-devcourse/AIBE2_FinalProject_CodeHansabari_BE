package com.cvmento.domain.auth.dto.response;

/**
 * 구글 로그인 안내 응답 DTO
 *
 * @param message 안내 메시지
 * @param loginUrl OAuth2 로그인 엔드포인트
 * @param fullUrl  전체 접속 URL
 * @param note     추가 안내 사항
 */
public record GoogleLoginGuideResponse(
        String message,
        String loginUrl,
        String fullUrl,
        String note
) {}
