package com.cvmento.domain.resume.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 이력서 썸네일 응답.
 *
 * @param resumeId 이력서 ID
 * @param title 이력서 제목
 * @param updatedAt 수정일시
 * @param completedSections 완료된 섹션 목록
 */
public record ResumeThumbnailResponse(
        Long resumeId,
        String title,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime updatedAt,
        List<String> completedSections
) {
    /**
     * 정적 팩토리 메서드
     */
    public static ResumeThumbnailResponse of(Long resumeId, String title,
                                             LocalDateTime updatedAt,
                                             List<String> completedSections) {
        return new ResumeThumbnailResponse(resumeId, title, updatedAt, completedSections);
    }
}