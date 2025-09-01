package com.cvmento.domain.resume.enums;

import lombok.Getter;

@Getter
public enum RecordStatus {
    ACTIVE("활성"),
    INACTIVE("비활성"),
    DELETED("삭제");

    private final String description;

    RecordStatus(String description) {
        this.description = description;
    }
}
