package com.cvmento.domain.resume.repository;

import com.cvmento.domain.resume.entity.TrainingTechStack;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TrainingTechStackRepository extends JpaRepository<TrainingTechStack, Long> {
}