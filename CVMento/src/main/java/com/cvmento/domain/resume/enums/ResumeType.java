package com.cvmento.domain.resume.enums;

import lombok.Getter;

@Getter
public enum ResumeType {
    DEFAULT("기본형"),
    MODERN("모던형");

    private final String description;

    ResumeType(String description) {
        this.description = description;
    }
}