package com.cvmento.domain.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;

// 테스트 로그인 요청
@Getter
@Builder
public class TestLoginRequest {
    @Email(message = "올바른 이메일 형식이 아닙니다.")
    @NotBlank(message = "이메일은 필수입니다.")
    private final String email;

    @NotBlank(message = "이름은 필수입니다.")
    private final String name;

    // 기본값 생성 메서드
    public static TestLoginRequest defaultRequest() {
        return TestLoginRequest.builder()
                .email("test@example.com")
                .name("Test User")
                .build();
    }
}