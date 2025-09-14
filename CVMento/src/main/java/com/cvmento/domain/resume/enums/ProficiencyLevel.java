package com.cvmento.domain.resume.enums;

import lombok.Getter;

/**
 * 기술 숙련도 enum.
 */
@Getter
public enum ProficiencyLevel {
    BEGINNER("초급"),
    INTERMEDIATE("중급"),
    ADVANCED("고급");

    private final String description;

    ProficiencyLevel(String description) {
        this.description = description;
    }
}
