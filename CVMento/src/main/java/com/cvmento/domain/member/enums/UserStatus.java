package com.cvmento.domain.member.enums;

import lombok.Getter;

@Getter
public enum UserStatus {
    ACTIVE("활성"),
    INACTIVE("비활성"),
    SUSPENDED("정지");

    private final String description;

    UserStatus(String description) {
        this.description = description;
    }

}