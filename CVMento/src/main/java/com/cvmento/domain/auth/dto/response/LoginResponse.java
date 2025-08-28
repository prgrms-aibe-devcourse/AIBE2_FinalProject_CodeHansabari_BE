package com.cvmento.domain.auth.dto.response;

import com.cvmento.domain.member.dto.MemberInfo;
import lombok.Builder;
import lombok.Getter;

// 로그인 응답
@Getter
@Builder
public class LoginResponse {
    private final String message;
    private final MemberInfo member;
    private final String note;
}

