package com.cvmento.global.config;

import com.cvmento.domain.member.entity.Member;
import org.springframework.data.domain.AuditorAware;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

public class AuditorAwareImpl implements AuditorAware<String> {

    @Override
    public @NonNull Optional<String> getCurrentAuditor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.of("system"); // 인증되지 않은 경우 기본값
        }

        // JWT 인증의 경우 Principal이 Member 객체
        Object principal = authentication.getPrincipal();

        if (principal instanceof Member member) {
            return Optional.of(member.getMemberId().toString());
        }

        // OAuth2 로그인 중인 경우나 기타 경우
        if (principal instanceof String) {
            return Optional.of((String) principal);
        }

        // 기본값 반환
        return Optional.of("anonymous");
    }
}