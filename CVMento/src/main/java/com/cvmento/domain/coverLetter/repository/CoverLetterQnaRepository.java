package com.cvmento.domain.coverLetter.repository;

import com.cvmento.domain.coverLetter.entity.CoverLetter;
import com.cvmento.domain.coverLetter.entity.CoverLetterQna;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CoverLetterQnaRepository extends JpaRepository<CoverLetterQna, Long> {
    List<CoverLetterQna> findByCoverLetter(CoverLetter coverLetter);
}
