package com.cvmento.domain.resume.repository;

import com.cvmento.domain.resume.entity.Career;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CareerRepository extends JpaRepository<Career, Long> {
}