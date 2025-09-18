package com.cvmento.domain.member.dto;

import com.cvmento.domain.member.entity.Member;
import java.time.LocalDateTime;

public record MemberDetailInfo(
        Long memberId,
        String email,
        String name,
        String picture,
        String role,
        String status,
        LocalDateTime createdAt,
        LocalDateTime lastLoginAt
) {
    public static MemberDetailInfo from(Member member) {
        return new MemberDetailInfo(
                member.getMemberId(),
                member.getEmail(),
                member.getName(),
                member.getPicture(),
                member.getRole().toString(),
                member.getStatus().toString(),
                member.getCreatedAt(),
                member.getLastLoginAt()
        );
    }
}