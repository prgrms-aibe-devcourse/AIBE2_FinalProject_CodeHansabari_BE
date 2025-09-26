package com.cvmento.domain.auth.service;

import com.cvmento.domain.member.enums.Role;
import com.cvmento.domain.auth.dto.TokenDto;
import com.cvmento.domain.member.entity.Member;
import com.cvmento.domain.member.repository.MemberRepository;
import com.cvmento.global.common.util.CookieUtil;
import com.cvmento.global.common.services.MetricsService;
import com.cvmento.global.security.JwtUtil;
import com.cvmento.global.security.TokenService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.MDC;

import java.time.Duration;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class AuthService {

    private final TokenService tokenService;
    private final MemberRepository memberRepository;
    private final CookieUtil cookieUtil;
    private final JwtUtil jwtUtil;
    private final MetricsService metricsService;

    /**
     * UserDetails 에서 Member 엔티티를 추출합니다.
     *
     * @param userDetails 인증된 사용자 정보
     * @return DB 에서 조회한 Member 엔티티
     * @throws IllegalArgumentException 사용자를 찾을 수 없는 경우
     */
    public Member getMemberFromUserDetails(UserDetails userDetails) {
        MDC.put("spanId", "auth-service");

        if (userDetails == null) {
            throw new IllegalArgumentException("UserDetails가 null 입니다.");
        }

        MDC.put("spanId", "member-repository");
        Member member = memberRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));

        MDC.put("spanId", "auth-service");
        return member;
    }

    /**
     * 현재 인증된 사용자가 활성 상태인지 확인합니다.
     *
     * @param userDetails 인증된 사용자 정보
     * @return 활성 상태이면 true
     */
    public boolean isUserAuthenticatedAndActive(UserDetails userDetails) {
        MDC.put("spanId", "auth-service");

        if (userDetails == null) {
            return false;
        }

        try {
            Member member = getMemberFromUserDetails(userDetails);
            return member.isActive();
        } catch (IllegalArgumentException e) {
            log.debug("사용자 확인 실패: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Refresh Token을 사용해 Access Token을 갱신합니다.
     *
     * @param request  클라이언트 요청
     * @param response 클라이언트 응답
     */
    @Transactional
    public void refreshAccessToken(HttpServletRequest request, HttpServletResponse response) {
        MDC.put("spanId", "token-refresh-service");

        Optional<String> refreshTokenOpt = cookieUtil.getRefreshTokenFromCookies(request);

        if (refreshTokenOpt.isEmpty()) {
            metricsService.incrementErrorCount("REFRESH_TOKEN_NOT_FOUND");
            throw new IllegalArgumentException("Refresh token not found");
        }

        String refreshToken = refreshTokenOpt.get();

        if (!jwtUtil.isValidToken(refreshToken)) {
            cookieUtil.deleteAllAuthCookies(response);
            metricsService.incrementErrorCount("INVALID_REFRESH_TOKEN_FORMAT");
            throw new IllegalArgumentException("Invalid refresh token format");
        }

        if (!jwtUtil.isRefreshToken(refreshToken)) {
            cookieUtil.deleteAllAuthCookies(response);
            metricsService.incrementErrorCount("WRONG_TOKEN_TYPE");
            throw new IllegalArgumentException("Wrong token type - not a refresh token");
        }

        if (jwtUtil.isTokenExpired(refreshToken)) {
            cookieUtil.deleteAllAuthCookies(response);
            metricsService.incrementErrorCount("REFRESH_TOKEN_EXPIRED");
            throw new IllegalArgumentException("Refresh token expired");
        }

        try {
            MDC.put("spanId", "token-service");
            TokenDto tokenDto = tokenService.refreshAccessToken(refreshToken);

            MDC.put("spanId", "token-refresh-service");
            cookieUtil.addAccessTokenCookie(response, tokenDto.accessToken(),
                    Duration.ofMillis(tokenService.getJwtUtil().getAccessTokenExpirationTime()));

            String userId = jwtUtil.extractUserId(refreshToken);
            log.info("토큰 갱신 성공 - userId: {}", userId);

        } catch (IllegalArgumentException e) {
            cookieUtil.deleteAllAuthCookies(response);
            log.debug("토큰 갱신 실패: {}", e.getMessage());
            metricsService.incrementErrorCount("REFRESH_TOKEN_VALIDATION_FAILED");
            throw new IllegalArgumentException("Refresh token validation failed");
        }
    }

    /**
     * 사용자 로그아웃 처리 (토큰 무효화 및 쿠키 삭제)
     */
    @Transactional
    public void logout(Member member, HttpServletRequest request, HttpServletResponse response) {
        MDC.put("spanId", "logout-service");

        String memberId = member.getMemberId().toString();
        Optional<String> accessToken = cookieUtil.getAccessTokenFromCookies(request);
        Optional<String> refreshToken = cookieUtil.getRefreshTokenFromCookies(request);

        MDC.put("spanId", "token-service");
        tokenService.logout(memberId, accessToken.orElse(null), refreshToken.orElse(null));

        MDC.put("spanId", "logout-service");
        cookieUtil.deleteAllAuthCookies(response);

        log.info("사용자 로그아웃 완료 - memberId: {}", member.getMemberId());
    }

    /**
     * 테스트 사용자 생성 또는 업데이트
     */
    @Transactional
    public Member createOrUpdateTestUser(String email, String name, Role role) {
        MDC.put("spanId", "test-user-service");

        MDC.put("spanId", "member-repository");
        Member testMember = memberRepository.findByEmail(email)
                .orElseGet(() -> {
                    Member newMember = new Member("test-google-id-" + System.currentTimeMillis(),
                            email, name, "https://via.placeholder.com/150");
                    log.info("테스트 사용자 생성 - role: {}", role);
                    return memberRepository.save(newMember);
                });

        MDC.put("spanId", "test-user-service");
        boolean needsUpdate = false;

        if (!testMember.getName().equals(name)) {
            testMember.updateProfile(name, testMember.getPicture());
            needsUpdate = true;
        }

        if (testMember.getRole() != role) {
            testMember.changeRole(role);
            needsUpdate = true;
        }

        if (needsUpdate) {
            MDC.put("spanId", "member-repository");
            testMember = memberRepository.save(testMember);
            MDC.put("spanId", "test-user-service");
            log.info("테스트 사용자 업데이트 - role: {}", role);
        }

        return testMember;
    }

    /**
     * 토큰을 생성하고 쿠키에 설정합니다.
     */
    public void generateTokensAndSetCookies(Member member, HttpServletResponse response) {
        MDC.put("spanId", "token-generation-service");

        TokenDto tokenDto = tokenService.generateTokens(member.getMemberId().toString(), member.getEmail());

        cookieUtil.addAccessTokenCookie(response, tokenDto.accessToken(),
                Duration.ofMillis(tokenService.getJwtUtil().getAccessTokenExpirationTime()));
        cookieUtil.addRefreshTokenCookie(response, tokenDto.refreshToken(),
                Duration.ofMillis(tokenService.getJwtUtil().getRefreshTokenExpirationTime()));

        metricsService.incrementLoginCount();
        log.debug("토큰 생성 완료 - memberId: {}", member.getMemberId());
    }
}