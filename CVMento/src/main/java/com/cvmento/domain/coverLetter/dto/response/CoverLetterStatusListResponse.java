package com.cvmento.domain.coverLetter.dto.response;

import com.cvmento.domain.coverLetter.enums.CoverLetterStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;

/**
 * 상태별 자소서 목록 응답 DTO
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record CoverLetterStatusListResponse(
        Long coverLetterId,
        String authorEmail,
        String title,
        CoverLetterStatus status,

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
    public CoverLetterStatusListResponse(
            Long coverLetterId,
            String authorEmail,
            String title,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            CoverLetterStatus status
    ) {
        this(
                coverLetterId,
                authorEmail,
                title,
                status,
                createdAt,
                updatedAt,
                status == CoverLetterStatus.DELETED ? updatedAt : null,
                status == CoverLetterStatus.DELETED ? updatedAt.plusDays(30) : null
        );
    }
}