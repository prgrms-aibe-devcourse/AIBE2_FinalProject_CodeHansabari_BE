package com.cvmento.domain.auth.dto.response;

import com.cvmento.domain.member.dto.MemberInfo;
import lombok.Builder;
import lombok.Getter;

// 인증 상태 응답
@Getter
@Builder
public class AuthStatusResponse {
    private final boolean authenticated;
    private final MemberInfo member;
}
