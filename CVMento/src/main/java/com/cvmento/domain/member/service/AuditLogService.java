package com.cvmento.domain.member.service;

import com.cvmento.domain.member.entity.Member;
import com.cvmento.domain.member.entity.MemberAuditLog;
import com.cvmento.domain.member.repository.MemberAuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AuditLogService {

    private final MemberAuditLogRepository auditLogRepository;

    public void logStatusChange(Member admin, Member target, String oldStatus,
                                String newStatus, String reason, String ipAddress) {
        MemberAuditLog auditLog = MemberAuditLog.statusChange(admin, target, oldStatus, newStatus, reason, ipAddress);
        auditLogRepository.save(auditLog);
        log.info("상태 변경 감사 로그 저장: admin={}, target={}, {}→{}",
                admin.getEmail(), target.getEmail(), oldStatus, newStatus);
    }

    public void logRoleChange(Member admin, Member target, String oldRole,
                              String newRole, String reason, String ipAddress) {
        MemberAuditLog auditLog = MemberAuditLog.roleChange(admin, target, oldRole, newRole, reason, ipAddress);
        auditLogRepository.save(auditLog);
        log.info("역할 변경 감사 로그 저장: admin={}, target={}, {}→{}",
                admin.getEmail(), target.getEmail(), oldRole, newRole);
    }

    public void logForceLogout(Member admin, Member target, String reason, String ipAddress) {
        MemberAuditLog auditLog = MemberAuditLog.forceLogout(admin, target, reason, ipAddress);
        auditLogRepository.save(auditLog);
        log.info("강제 로그아웃 감사 로그 저장: admin={}, target={}", admin.getEmail(), target.getEmail());
    }
}