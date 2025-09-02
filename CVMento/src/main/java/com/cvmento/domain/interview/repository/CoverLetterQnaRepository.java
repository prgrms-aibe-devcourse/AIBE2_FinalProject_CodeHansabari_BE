package com.cvmento.domain.interview.repository;

import com.cvmento.domain.coverLetter.entity.CoverLetter;
import com.cvmento.domain.interview.entity.CoverLetterQna;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CoverLetterQnaRepository extends JpaRepository<CoverLetterQna, Long> {
    List<CoverLetterQna> findByCoverLetter(CoverLetter coverLetter);
    List<CoverLetterQna> findByCoverLetterOrderByCreatedAtAsc(CoverLetter coverLetter);
}
