package com.cvmento.domain.member.dto.response;

import lombok.Builder;

@Builder
public record MemberStatisticsResponse(
        long totalMembers,
        long activeMembers,
        long inactiveMembers,
        long suspendedMembers,
        long userRoleCount,
        long adminRoleCount,
        long rootRoleCount,
        long todayNewMembers,
        long monthlyNewMembers
) {}