package com.cvmento.domain.auth.dto;

/**
 * Google 사용자 정보 DTO
 *
 * @param googleId Google 사용자 고유 ID (sub)
 * @param email    사용자 이메일
 * @param name     사용자 이름
 * @param picture  프로필 사진 URL (선택적)
 */
public record GoogleUserInfo(
        String googleId,
        String email,
        String name,
        String picture
) {
}