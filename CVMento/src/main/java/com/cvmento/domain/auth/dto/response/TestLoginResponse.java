package com.cvmento.domain.auth.dto.response;

import com.cvmento.domain.member.dto.MemberInfo;
import lombok.Builder;
import lombok.Getter;

// 테스트 로그인 응답
@Getter
@Builder
public class TestLoginResponse {
    private final String message;
    private final MemberInfo member;
    private final String note;

    public static TestLoginResponse of(String message, MemberInfo member, String note) {
        return TestLoginResponse.builder()
                .message(message)
                .member(member)
                .note(note)
                .build();
    }
}
