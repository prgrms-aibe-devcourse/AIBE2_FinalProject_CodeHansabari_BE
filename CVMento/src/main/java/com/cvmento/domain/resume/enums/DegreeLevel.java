package com.cvmento.domain.resume.enums;

import lombok.Getter;

@Getter
public enum DegreeLevel {
    HIGH_SCHOOL("고졸"),
    ASSOCIATE("전문학사"),
    BACHELOR("학사"),
    MASTER("석사"),
    DOCTORATE("박사");

    private final String description;

    DegreeLevel(String description) {
        this.description = description;
    }
}