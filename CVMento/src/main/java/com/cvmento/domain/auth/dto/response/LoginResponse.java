package com.cvmento.domain.auth.dto.response;

import com.cvmento.domain.member.dto.MemberInfo;

/**
 * 로그인 응답 DTO
 *
 * @param message 응답 메시지
 * @param member  로그인한 사용자 정보
 * @param note    부가 설명
 */
public record LoginResponse(
        String message,
        MemberInfo member,
        String note
) {}
