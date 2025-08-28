package com.cvmento.domain.member.dto;

import com.cvmento.domain.member.entity.Member;

public record MemberInfo(
        Long memberId,
        String email,
        String name,
        String picture
) {
    public static MemberInfo from(Member member) {
        return new MemberInfo(
                member.getMemberId(),
                member.getEmail(),
                member.getName(),
                member.getPicture()
        );
    }
}