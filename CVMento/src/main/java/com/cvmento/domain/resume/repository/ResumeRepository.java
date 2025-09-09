package com.cvmento.domain.resume.repository;

import com.cvmento.domain.resume.entity.Resume;
import com.cvmento.domain.resume.enums.ResumeStatus;
import com.cvmento.domain.member.entity.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ResumeRepository extends JpaRepository<Resume, Long>, ResumeRepositoryCustom {

    // 기본 조회 (권한 확인)
    Optional<Resume> findByIdAndMember(Long id, Member member);

    // 상태별 조회 (권한 + 상태 확인) - 간단한 JPA 메서드
    Optional<Resume> findByIdAndMemberAndStatus(Long id, Member member, ResumeStatus status);

    Page<Resume> findByMemberEmailAndStatusOrderByUpdatedAtDesc(String email,
                                                                ResumeStatus status,
                                                                Pageable pageable);
}