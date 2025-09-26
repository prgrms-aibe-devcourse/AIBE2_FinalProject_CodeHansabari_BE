package com.cvmento.domain.member.entity;

import com.cvmento.domain.member.enums.Role;
import com.cvmento.domain.member.enums.UserStatus;
import com.cvmento.global.common.entity.BaseTimeEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;

import java.time.LocalDateTime;

@Entity
@Getter
@Table(name = "members")
public class Member extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private Long memberId;

    @Column(unique = true, nullable = false)
    private String googleId;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "profile_img")
    private String picture;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @Column(name = "role", nullable = false)
    @Enumerated(EnumType.STRING)
    private Role role;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private UserStatus status;

    public Member() {}

    public Member(String googleId, String email, String name, String picture) {
        this.googleId = googleId;
        this.email = email;
        this.name = name;
        this.picture = picture;
        this.role = Role.USER;
        this.status = UserStatus.ACTIVE;
    }

    public void deactivate() {
        this.status = UserStatus.INACTIVE;
    }

    public void activate() {
        this.status = UserStatus.ACTIVE;
    }

    @JsonIgnore
    public boolean isActive() {
        return this.status == UserStatus.ACTIVE;
    }

    public void updateProfile(String name, String picture) {
        this.name = name;
        this.picture = picture;
    }

    public void changeRole(Role role) {
        this.role = role;
    }

    public void updateLastLoginAt(LocalDateTime time) {
        this.lastLoginAt = time;
    }
    
}