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
    
    /** DTO에서 엔티티로 변환 */
    public CrawlCoverLetter toEntity() {
        // DB에서 조회된 텍스트는 이미 cleanText()로 정리되어 있음
        CrawlCoverLetter entity = new CrawlCoverLetter(this.text);
        
        // ID가 있는 경우에만 설정 (기존 데이터 조회 시)
        if (this.coverLetterId != null) {
            // 리플렉션을 사용하여 ID 설정
            try {
                java.lang.reflect.Field idField = CrawlCoverLetter.class.getDeclaredField("coverLetterId");
                idField.setAccessible(true);
                idField.set(entity, this.coverLetterId);
            } catch (Exception e) {
                throw new RuntimeException("ID 설정 실패", e);
            }
        }
        return entity;
    }
}
