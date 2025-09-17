package com.cvmento.domain.member.dto.request;

import com.cvmento.domain.member.enums.UserStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record MemberStatusUpdateRequest(
        @NotNull(message = "상태는 필수입니다.")
        UserStatus status,

        @Size(max = 500, message = "사유는 500자 이하로 입력해주세요.")
        String reason
) {}