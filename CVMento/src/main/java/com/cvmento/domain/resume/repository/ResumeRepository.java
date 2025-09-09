package com.cvmento.domain.resume.repository;

import com.cvmento.domain.resume.entity.Resume;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ResumeRepository extends JpaRepository<Resume, Long> {

    @EntityGraph(attributePaths = {"member", "sections", "sections.items"})
    List<Resume> findByMember_MemberId(Long memberId);
    
    @EntityGraph(attributePaths = {"member", "sections", "sections.items"})
    Optional<Resume> findByResumeIdAndMember_Email(Long resumeId, String email);
    
    @EntityGraph(attributePaths = {"member", "sections", "sections.items"})
    @Query("SELECT r FROM Resume r WHERE r.resumeId = :resumeId")
    Optional<Resume> findByIdWithAssociations(@Param("resumeId") Long resumeId);
}
