package com.cvmento.domain.resume.repository;

import com.cvmento.domain.resume.entity.TechStack;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TechStackRepository extends JpaRepository<TechStack, Long> {
    List<TechStack> findAllByOrderByNameAsc();
    
    Optional<TechStack> findByName(String name);
}