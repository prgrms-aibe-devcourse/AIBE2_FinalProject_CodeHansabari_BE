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
public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByGoogleId(String googleId);

    Optional<Member> findByEmail(String email);

    @Query("SELECT m FROM Member m WHERE m.googleId = :googleId AND m.status = 'ACTIVE'")
    Optional<Member> findActiveByGoogleId(@Param("googleId") String googleId);

    @Query("SELECT m FROM Member m WHERE m.email = :email AND m.status = 'ACTIVE'")
    Optional<Member> findActiveByEmail(@Param("email") String email);

    boolean existsByEmail(String email);

    boolean existsByGoogleId(String googleId);

    // ================ 관리자용 통계 쿼리 메서드들 ================

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

    /**
     * 최근 로그인 기준 활성 사용자 조회
     */
    @Query("SELECT COUNT(m) FROM Member m WHERE m.lastLoginAt >= :since AND m.status = 'ACTIVE'")
    long countActiveUsersSince(@Param("since") LocalDateTime since);

    /**
     * 역할별 활성 사용자 수 조회
     */
    @Query("SELECT COUNT(m) FROM Member m WHERE m.role = :role AND m.status = 'ACTIVE'")
    long countActiveByRole(@Param("role") Role role);

    /**
     * 이메일 도메인별 통계 (필요시 사용)
     */
    @Query("SELECT SUBSTRING(m.email, LOCATE('@', m.email) + 1) as domain, COUNT(m) as count " +
            "FROM Member m GROUP BY SUBSTRING(m.email, LOCATE('@', m.email) + 1) " +
            "ORDER BY count DESC")
    java.util.List<Object[]> getEmailDomainStatistics();

    List<Member> findByStatus(UserStatus status);
}