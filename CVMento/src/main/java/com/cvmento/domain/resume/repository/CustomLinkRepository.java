package com.cvmento.domain.resume.repository;

import com.cvmento.domain.resume.entity.CustomLink;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomLinkRepository extends JpaRepository<CustomLink, Long> {
}