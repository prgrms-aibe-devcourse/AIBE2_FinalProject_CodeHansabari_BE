package com.cvmento.domain.coverLetter.dto.response;

import com.cvmento.domain.coverLetter.entity.CrawlCoverLetter;

import java.time.LocalDateTime;

/**
 * 크롤링된 자소서 데이터 응답 DTO
 */
public record CrawlCoverLetterDto(
    Long coverLetterId,
    String text,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    
    /**
     * 엔티티로부터 DTO 생성
     */
    public static CrawlCoverLetterDto from(CrawlCoverLetter entity) {
        return new CrawlCoverLetterDto(
            entity.getCoverLetterId(),
            entity.getText(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }
}
