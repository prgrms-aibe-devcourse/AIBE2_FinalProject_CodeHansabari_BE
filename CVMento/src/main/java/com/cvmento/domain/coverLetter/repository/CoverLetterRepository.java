package com.cvmento.domain.coverLetter.repository;

import com.cvmento.domain.coverLetter.entity.CoverLetter;
import com.cvmento.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CoverLetterRepository extends JpaRepository<CoverLetter, Long> {
    List<CoverLetter> findByMember(Member member);
    List<CoverLetter> findAllByOrderByUpdatedAtDesc();
}
