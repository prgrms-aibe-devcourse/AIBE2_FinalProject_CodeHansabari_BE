package com.cvmento.domain.resume.enums;

import lombok.Getter;

@Getter
public enum CareerType {
    FRESHMAN("신입"),
    EXPERIENCED("경력");

    private final String description;

    CareerType(String description) {
        this.description = description;
    }
}