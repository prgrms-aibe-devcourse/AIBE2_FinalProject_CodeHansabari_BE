package com.cvmento.domain.resume.enums;

import lombok.Getter;

@Getter
public enum AdditionalInfoCategory {
    AWARD("수상내역"),
    LANGUAGE("어학능력"),
    CERTIFICATE("자격증"),
    ACTIVITY("대외활동");

    private final String description;

    AdditionalInfoCategory(String description) {
        this.description = description;
    }
}