package com.cvmento.domain.coverLetter.repository;

import com.cvmento.domain.coverLetter.entity.CrawlCoverLetter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CrawlCoverLetterRepository extends JpaRepository<CrawlCoverLetter, Long> {
    
    /**
     * 모든 크롤링 데이터를 생성일 기준 내림차순으로 조회
     */
    List<CrawlCoverLetter> findAllByOrderByCreatedAtDesc();
    
    /**
     * 최소 ID 조회
     */
    @Query("SELECT MIN(c.coverLetterId) FROM CrawlCoverLetter c")
    Long findMinId();
    
    
    /**
     * 페이징을 사용한 크롤링 데이터 조회 (생성일 기준 내림차순)
     */
    Page<CrawlCoverLetter> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
