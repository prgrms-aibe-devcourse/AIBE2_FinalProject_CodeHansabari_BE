package com.cvmento.domain.interview.repository;

import com.cvmento.domain.coverLetter.entity.CoverLetter;
import com.cvmento.domain.interview.entity.CoverLetterQna;
import com.cvmento.domain.interview.enums.QuestionSourceType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CoverLetterQnaRepository extends JpaRepository<CoverLetterQna, Long>{
    List<CoverLetterQna> findByCoverLetterOrderByCreatedAtAsc(CoverLetter coverLetter);

    long countByCoverLetterAndSourceType(CoverLetter coverLetter, QuestionSourceType sourceType);

    @Query("SELECT q.question FROM CoverLetterQna q WHERE q.coverLetter = :coverLetter AND q.sourceType = :sourceType ORDER BY q.createdAt ASC")
    List<String> findQuestionsByCoverLetterAndSourceType(@Param("coverLetter") CoverLetter coverLetter,
                                                         @Param("sourceType") QuestionSourceType sourceType);
}
