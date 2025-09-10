package com.cvmento.domain.resume.enums;

import lombok.Getter;

@Getter
public enum ProjectType {
    PERSONAL("개인"),
    TEAM("팀"),
    COMPANY("회사");

    private final String description;

    ProjectType(String description) {
        this.description = description;
    }
}