package com.cvmento.domain.coverLetter.repository;

import com.cvmento.domain.coverLetter.entity.CrawlCoverLetter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface CrawlCoverLetterRepository extends JpaRepository<CrawlCoverLetter, Long> {
    
    /**
     * 최소 ID 조회
     */
    @Query("SELECT MIN(c.coverLetterId) FROM CrawlCoverLetter c")
    Long findMinId();
}
