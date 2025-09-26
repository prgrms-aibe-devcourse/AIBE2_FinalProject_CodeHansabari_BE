package com.cvmento.domain.member.repository;

import com.cvmento.domain.member.entity.Member;
import com.cvmento.domain.member.enums.Role;
import com.cvmento.domain.member.enums.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long>, JpaSpecificationExecutor<Member> {

    Optional<Member> findByGoogleId(String googleId);

    Optional<Member> findByEmail(String email);

    long countByEmailStartingWith(String prefix);

    /**
     * 상태별 회원 수 조회
     */
    long countByStatus(UserStatus status);

    /**
     * 역할별 회원 수 조회
     */
    long countByRole(Role role);

    /**
     * 기간별 가입 회원 수 조회
     */
    long countByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    long countByCreatedAtAfter(LocalDateTime startDate);

    List<Member> findByStatus(UserStatus status);

    @Query("SELECT COUNT(c) FROM CoverLetter c WHERE c.member = :member")
    long countCoverLettersByMember(@Param("member") Member member);

    @Query("SELECT COUNT(r) FROM Resume r WHERE r.member = :member")
    long countResumesByMember(@Param("member") Member member);
}