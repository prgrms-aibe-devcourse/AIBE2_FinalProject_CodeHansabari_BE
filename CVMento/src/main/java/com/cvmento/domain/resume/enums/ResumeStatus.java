package com.cvmento.domain.resume.enums;

import lombok.Getter;

/**
 * 이력서 상태 enum.
 */
@Getter
public enum ResumeStatus {
    ACTIVE("활성"),
    DELETED("삭제");

    private final String description;

    ResumeStatus(String description) {
        this.description = description;
    }
}