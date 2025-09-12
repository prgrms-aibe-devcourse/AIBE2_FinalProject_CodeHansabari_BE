package com.cvmento.domain.auth.enums;

import lombok.Getter;

/**
 * JWT 토큰의 종류를 나타내는 열거형
 */
@Getter
public enum TokenType {
    ACCESS("access"),
    REFRESH("refresh");

    private final String type;

    TokenType(String type) {
        this.type = type;
    }

}