package com.cvmento.domain.coverLetter.repository;

import com.cvmento.domain.coverLetter.entity.CoverLetter;
import com.cvmento.domain.coverLetter.enums.CoverLetterStatus;
import com.cvmento.domain.member.entity.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface CoverLetterRepository extends JpaRepository<CoverLetter, Long>, CoverLetterRepositoryCustom {
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

    // 스케줄러용 - 상태와 수정일 기준으로 자소서 조회
    List<CoverLetter> findByStatusAndUpdatedAtBefore(
            CoverLetterStatus status,
            LocalDateTime cutoffDate
    );

    //관리자용 - ID와 상태로만 자소서 조회 (복구용)
    Optional<CoverLetter> findByCoverLetterIdAndStatus(
            Long coverLetterId,
            CoverLetterStatus status
    );
}