package com.cvmento.domain.resume.repository;

import com.cvmento.domain.resume.entity.ResumeItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ResumeItemRepository extends JpaRepository<ResumeItem, Long> {
}