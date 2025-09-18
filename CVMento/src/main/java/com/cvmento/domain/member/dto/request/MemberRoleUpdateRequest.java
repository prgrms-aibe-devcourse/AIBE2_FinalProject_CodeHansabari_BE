package com.cvmento.domain.member.dto.request;

import com.cvmento.domain.member.enums.Role;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record MemberRoleUpdateRequest(
        @NotNull(message = "역할은 필수입니다.")
        Role role,

        @Size(max = 500, message = "사유는 500자 이하로 입력해주세요.")
        String reason
) {}