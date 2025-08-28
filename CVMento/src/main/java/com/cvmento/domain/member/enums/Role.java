package com.cvmento.domain.member.enums;

import org.springframework.security.core.GrantedAuthority;

import java.util.List;

public enum Role {
    USER, // 일반 사용자
    ADMIN, // 관리자
    ROOT; // 최고 관리자

    public List<GrantedAuthority> getAuthorities() {
        return List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_" + this.name()));
    }
}
