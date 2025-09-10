package com.cvmento.domain.coverLetter.repository;

import com.cvmento.domain.coverLetter.entity.CoverLetter;
import com.cvmento.domain.coverLetter.enums.CoverLetterStatus;
import com.cvmento.domain.member.entity.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CoverLetterRepository extends JpaRepository<CoverLetter, Long> {
    List<CoverLetter> findByMember(Member member);
    List<CoverLetter> findAllByOrderByUpdatedAtDesc();

    // 페이징 지원 메서드들
    Page<CoverLetter> findByMemberAndStatusOrderByUpdatedAtDesc(Member member, CoverLetterStatus status, Pageable pageable);

    @Query("SELECT c FROM CoverLetter c WHERE c.coverLetterId = :coverLetterId AND c.member.email = :memberEmail AND c.status = :status")
    Optional<CoverLetter> findByCoverLetterIdAndMemberEmailAndStatus(
            @Param("coverLetterId") Long coverLetterId,
            @Param("memberEmail") String memberEmail,
            @Param("status") CoverLetterStatus status
    );
}