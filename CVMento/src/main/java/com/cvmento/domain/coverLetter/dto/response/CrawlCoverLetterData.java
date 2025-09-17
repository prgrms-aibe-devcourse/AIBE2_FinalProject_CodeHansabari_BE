package com.cvmento.domain.coverLetter.dto.response;

import com.cvmento.domain.coverLetter.entity.CrawlCoverLetter;

import java.time.LocalDateTime;

/**
 * 크롤링된 자소서 데이터 응답 DTO
 *
 * @param coverLetterId 자소서 ID
 * @param text          자소서 본문
 * @param createdAt     생성일
 * @param updatedAt     수정일
 */
public record CrawlCoverLetterData(
        Long coverLetterId,
        String text,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    /** 엔티티로부터 DTO 변환 */
    public static CrawlCoverLetterData from(CrawlCoverLetter entity) {
        return new CrawlCoverLetterData(
                entity.getCoverLetterId(),
                entity.getText(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }
}
