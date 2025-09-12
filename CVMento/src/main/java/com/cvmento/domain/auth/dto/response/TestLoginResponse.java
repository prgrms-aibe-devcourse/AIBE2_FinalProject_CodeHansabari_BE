package com.cvmento.domain.auth.dto.response;

import com.cvmento.domain.member.dto.MemberInfo;

/**
 * 테스트 로그인 응답 DTO
 *
 * @param message 응답 메시지
 * @param member  로그인된 사용자 정보
 * @param note    추가 설명
 */
public record TestLoginResponse(
        String message,
        MemberInfo member,
        String note
) {
    public static TestLoginResponse of(String message, MemberInfo member, String note) {
        return new TestLoginResponse(message, member, note);
    }
}
