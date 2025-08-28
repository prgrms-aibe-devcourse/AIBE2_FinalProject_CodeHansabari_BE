package com.cvmento.domain.member.repository;

import com.cvmento.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByGoogleId(String googleId);

    Optional<Member> findByEmail(String email);

    @Query("SELECT m FROM Member m WHERE m.googleId = :googleId AND m.status = 'ACTIVE'")
    Optional<Member> findActiveByGoogleId(@Param("googleId") String googleId);

    @Query("SELECT m FROM Member m WHERE m.email = :email AND m.status = 'ACTIVE'")
    Optional<Member> findActiveByEmail(@Param("email") String email);

    boolean existsByEmail(String email);

    boolean existsByGoogleId(String googleId);
}