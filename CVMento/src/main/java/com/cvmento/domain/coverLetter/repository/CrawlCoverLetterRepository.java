package com.cvmento.domain.coverLetter.repository;

import com.cvmento.domain.coverLetter.entity.CrawlCoverLetter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CrawlCoverLetterRepository extends JpaRepository<CrawlCoverLetter, Long> {
}
