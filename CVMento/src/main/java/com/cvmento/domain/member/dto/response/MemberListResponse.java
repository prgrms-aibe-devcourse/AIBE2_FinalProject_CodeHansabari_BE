package com.cvmento.domain.member.dto.response;

import com.cvmento.domain.member.entity.Member;
import com.cvmento.domain.member.enums.Role;
import com.cvmento.domain.member.enums.UserStatus;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record MemberListResponse(
        Long memberId,
        String email,
        String name,
        String picture,
        Role role,
        UserStatus status,
        LocalDateTime lastLoginAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static MemberListResponse from(Member member) {
        return MemberListResponse.builder()
                .memberId(member.getMemberId())
                .email(member.getEmail())
                .name(member.getName())
                .picture(member.getPicture())
                .role(member.getRole())
                .status(member.getStatus())
                .lastLoginAt(member.getLastLoginAt())
                .createdAt(member.getCreatedAt())
                .updatedAt(member.getUpdatedAt())
                .build();
    }
}
