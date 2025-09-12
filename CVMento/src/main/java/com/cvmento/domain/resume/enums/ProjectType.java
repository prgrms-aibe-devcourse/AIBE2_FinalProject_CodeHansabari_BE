package com.cvmento.domain.resume.enums;

import lombok.Getter;

/**
 * 프로젝트 타입 enum.
 */
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