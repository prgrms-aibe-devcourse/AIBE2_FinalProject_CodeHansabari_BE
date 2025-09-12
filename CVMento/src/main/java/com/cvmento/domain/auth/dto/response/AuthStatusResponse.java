package com.cvmento.domain.auth.dto.response;

import com.cvmento.domain.member.dto.MemberInfo;

/**
 * 인증 상태 응답 DTO
 *
 * @param authenticated 인증 여부
 * @param member        인증된 사용자 정보
 */
public record AuthStatusResponse(
        boolean authenticated,
        MemberInfo member
) {}
