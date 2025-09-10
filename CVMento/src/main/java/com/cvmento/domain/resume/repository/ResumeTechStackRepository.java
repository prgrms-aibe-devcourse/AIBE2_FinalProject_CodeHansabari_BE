package com.cvmento.domain.resume.repository;

import com.cvmento.domain.resume.entity.ResumeTechStack;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ResumeTechStackRepository extends JpaRepository<ResumeTechStack, Long> {
}