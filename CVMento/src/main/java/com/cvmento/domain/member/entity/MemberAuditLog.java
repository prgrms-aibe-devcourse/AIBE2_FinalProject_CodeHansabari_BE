package com.cvmento.domain.member.entity;

import com.cvmento.global.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Getter
@Table(name = "member_audit_logs")
public class MemberAuditLog extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "audit_id")
    private Long auditId;

    @Column(name = "admin_member_id", nullable = false)
    private Long adminMemberId;

    @Column(name = "admin_email", nullable = false)
    private String adminEmail;

    @Column(name = "target_member_id", nullable = false)
    private Long targetMemberId;

    @Column(name = "target_email", nullable = false)
    private String targetEmail;

    @Column(name = "action", nullable = false)
    private String action; // STATUS_CHANGE, ROLE_CHANGE, FORCE_LOGOUT

    @Column(name = "old_value")
    private String oldValue;

    @Column(name = "new_value")
    private String newValue;

    @Column(name = "reason", columnDefinition = "TEXT")
    private String reason;

    @Column(name = "ip_address")
    private String ipAddress;

    protected MemberAuditLog() {}

    public MemberAuditLog(Long adminMemberId, String adminEmail, Long targetMemberId,
                          String targetEmail, String action, String oldValue,
                          String newValue, String reason, String ipAddress) {
        this.adminMemberId = adminMemberId;
        this.adminEmail = adminEmail;
        this.targetMemberId = targetMemberId;
        this.targetEmail = targetEmail;
        this.action = action;
        this.oldValue = oldValue;
        this.newValue = newValue;
        this.reason = reason;
        this.ipAddress = ipAddress;
    }

    // 정적 팩토리 메서드들
    public static MemberAuditLog statusChange(Member admin, Member target, String oldStatus,
                                              String newStatus, String reason, String ipAddress) {
        return new MemberAuditLog(
                admin.getMemberId(), admin.getEmail(),
                target.getMemberId(), target.getEmail(),
                "STATUS_CHANGE", oldStatus, newStatus, reason, ipAddress
        );
    }

    public static MemberAuditLog roleChange(Member admin, Member target, String oldRole,
                                            String newRole, String reason, String ipAddress) {
        return new MemberAuditLog(
                admin.getMemberId(), admin.getEmail(),
                target.getMemberId(), target.getEmail(),
                "ROLE_CHANGE", oldRole, newRole, reason, ipAddress
        );
    }

    public static MemberAuditLog forceLogout(Member admin, Member target, String reason, String ipAddress) {
        return new MemberAuditLog(
                admin.getMemberId(), admin.getEmail(),
                target.getMemberId(), target.getEmail(),
                "FORCE_LOGOUT", null, null, reason, ipAddress
        );
    }
}
