package com.cvmento.domain.resume.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;
import java.util.List;

public record ResumeThumbnailResponse(
        Long resumeId,
        String title,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime updatedAt,
        List<String> completedSections
) {
    public static ResumeThumbnailResponse of(Long resumeId, String title,
                                             LocalDateTime updatedAt,
                                             List<String> completedSections) {
        return new ResumeThumbnailResponse(resumeId, title, updatedAt, completedSections);
    }
}