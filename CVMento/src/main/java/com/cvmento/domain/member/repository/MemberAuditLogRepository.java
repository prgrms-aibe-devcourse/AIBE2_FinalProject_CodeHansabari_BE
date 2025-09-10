package com.cvmento.domain.member.repository;

import com.cvmento.domain.member.entity.MemberAuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MemberAuditLogRepository extends JpaRepository<MemberAuditLog, Long> {

    /**
     * 특정 회원에 대한 감사 로그 조회
     */
    Page<MemberAuditLog> findByTargetMemberIdOrderByCreatedAtDesc(Long targetMemberId, Pageable pageable);

    /**
     * 특정 관리자의 행동 로그 조회
     */
    Page<MemberAuditLog> findByAdminMemberIdOrderByCreatedAtDesc(Long adminMemberId, Pageable pageable);

    /**
     * 기간별 감사 로그 조회
     */
    @Query("SELECT a FROM MemberAuditLog a WHERE a.createdAt BETWEEN :startDate AND :endDate ORDER BY a.createdAt DESC")
    List<MemberAuditLog> findByCreatedAtBetween(@Param("startDate") LocalDateTime startDate,
                                                @Param("endDate") LocalDateTime endDate);

    /**
     * 액션별 로그 조회
     */
    Page<MemberAuditLog> findByActionOrderByCreatedAtDesc(String action, Pageable pageable);
}