package com.cvmento.domain.resume.repository;

import com.cvmento.domain.resume.entity.Resume;
import com.cvmento.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ResumeRepository extends JpaRepository<Resume, Long>, ResumeRepositoryCustom {

    Optional<Resume> findByIdAndMember(Long id, Member member);
}