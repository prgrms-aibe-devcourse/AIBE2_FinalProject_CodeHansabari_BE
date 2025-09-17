package com.cvmento.domain.member.dto.response;

import com.cvmento.domain.member.entity.Member;
import com.cvmento.domain.member.enums.Role;
import com.cvmento.domain.member.enums.UserStatus;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record MemberDetailResponse(
        Long memberId,
        String googleId,
        String email,
        String name,
        String picture,
        Role role,
        UserStatus status,
        LocalDateTime lastLoginAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        int coverLetterCount,
        int resumeCount
) {
    public static MemberDetailResponse from(Member member, int coverLetterCount, int resumeCount) {
        return MemberDetailResponse.builder()
                .memberId(member.getMemberId())
                .googleId(member.getGoogleId())
                .email(member.getEmail())
                .name(member.getName())
                .picture(member.getPicture())
                .role(member.getRole())
                .status(member.getStatus())
                .lastLoginAt(member.getLastLoginAt())
                .createdAt(member.getCreatedAt())
                .updatedAt(member.getUpdatedAt())
                .coverLetterCount(coverLetterCount)
                .resumeCount(resumeCount)
                .build();
    }
}
