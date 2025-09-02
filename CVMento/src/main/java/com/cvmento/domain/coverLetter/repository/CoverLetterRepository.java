package com.cvmento.domain.coverLetter.repository;

import com.cvmento.domain.coverLetter.entity.CoverLetter;
import com.cvmento.domain.member.entity.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CoverLetterRepository extends JpaRepository<CoverLetter, Long> {
    List<CoverLetter> findByMember(Member member);
    List<CoverLetter> findAllByOrderByUpdatedAtDesc();

    // 페이징 지원 메서드들
    Page<CoverLetter> findByMemberOrderByUpdatedAtDesc(Member member, Pageable pageable);
    Optional<CoverLetter> findByCoverLetterIdAndMember(Long coverLetterId, Member member);
}