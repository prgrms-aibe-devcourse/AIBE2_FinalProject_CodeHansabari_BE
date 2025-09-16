package com.cvmento.domain.resume.dto.response;

import com.cvmento.domain.resume.enums.ResumeStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;

/**
 * 상태별 이력서 목록 응답 DTO
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ResumeStatusListResponse(
        Long resumeId,
        String authorEmail,
        String title,
        ResumeStatus status,

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime createdAt,

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime updatedAt,

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime deletedAt,

        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime scheduledDeletionDate
) {

    /**
     * QueryDSL용 6개 파라미터 생성자 (상태에 따라 삭제 관련 필드 처리)
     */
    public ResumeStatusListResponse(
            Long resumeId,
            String authorEmail,
            String title,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            ResumeStatus status
    ) {
        this(
                resumeId,
                authorEmail,
                title,
                status,
                createdAt,
                updatedAt,
                status == ResumeStatus.DELETED ? updatedAt : null,
                status == ResumeStatus.DELETED ? updatedAt.plusDays(30) : null
        );
    }
}