package com.cvmento.domain.resume.repository;

import com.cvmento.domain.resume.entity.Resume;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ResumeRepository extends JpaRepository<Resume, Long> {

    List<Resume> findByMember_MemberId(Long memberId);
}
