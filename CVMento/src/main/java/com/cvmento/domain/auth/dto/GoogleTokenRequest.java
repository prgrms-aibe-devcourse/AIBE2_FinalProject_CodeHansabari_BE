package com.cvmento.domain.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GoogleTokenRequest {

    @NotBlank(message = "Google ID Token은 필수입니다.")
    private String idToken;

    // 기본 생성자
    public GoogleTokenRequest() {}

    // 생성자
    public GoogleTokenRequest(String idToken) {
        this.idToken = idToken;
    }
}