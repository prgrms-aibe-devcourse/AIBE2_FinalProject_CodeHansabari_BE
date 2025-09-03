package com.cvmento.domain.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GoogleLoginRequest {

    @NotBlank(message = "Authorization code는 필수입니다.")
    private String code;

    private String state;

    private String redirectUri;

    public GoogleLoginRequest() {}

    public GoogleLoginRequest(String code, String state, String redirectUri) {
        this.code = code;
        this.state = state;
        this.redirectUri = redirectUri;
    }
}