package com.cvmento.global.usage.service;

import com.cvmento.domain.member.entity.Member;
import com.cvmento.domain.member.enums.UserStatus;
import com.cvmento.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 신규 사용자 토큰 초기화 (고정 시점 충전 방식)
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class NewUserTokenInitializer {

    private final UsageTokenService usageTokenService;
    private final MemberRepository memberRepository;

    /**
     * 애플리케이션 시작 시 기존 활성 사용자들의 토큰 초기화
     */
    @EventListener(ApplicationReadyEvent.class)
    public void initializeExistingUsersTokens() {
        MDC.put("spanId", "app-startup-token-init");

        try {
            MDC.put("spanId", "member-repository");
            List<Member> activeMembers = memberRepository.findByStatus(UserStatus.ACTIVE);

            MDC.put("spanId", "app-startup-token-init");
            log.info("기존 활성 사용자 토큰 초기화 시작 - 대상 사용자 수: {}", activeMembers.size());

            int successCount = 0;
            for (Member member : activeMembers) {
                try {
                    // 사용자별 토큰 초기화 시도
                    usageTokenService.initializeUserTokens(member.getMemberId());
                    successCount++;
                } catch (Exception e) {
                    log.error("사용자 토큰 초기화 실패 - 사용자 ID: {}, 에러: {}",
                            member.getMemberId(), e.getMessage());
                }
            }

            log.info("기존 사용자 토큰 초기화 완료 - 성공: {}/{}", successCount, activeMembers.size());

        } catch (Exception e) {
            log.error("기존 사용자 토큰 초기화 중 오류 발생", e);
        }
    }

    /**
     * 신규 가입 사용자 토큰 초기화
     * 회원가입 시점에 호출
     */
    public void initializeNewUserTokens(Long userId) {
        MDC.put("spanId", "new-user-token-init");

        try {
            usageTokenService.initializeUserTokens(userId);
            log.info("신규 사용자 토큰 초기화 완료 - 사용자 ID: {}", userId);
        } catch (Exception e) {
            log.error("신규 사용자 토큰 초기화 실패 - 사용자 ID: {}, 에러: {}", userId, e.getMessage());
        }
    }
}