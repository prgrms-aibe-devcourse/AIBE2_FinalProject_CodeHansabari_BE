package com.cvmento.domain.auth.service;

import com.cvmento.domain.member.enums.Role;
import com.cvmento.domain.auth.dto.TokenDto;
import com.cvmento.domain.member.entity.Member;
import com.cvmento.domain.member.repository.MemberRepository;
import com.cvmento.global.common.util.CookieUtil;
import com.cvmento.global.security.JwtUtil;
import com.cvmento.global.security.TokenService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    /**
     * UserDetails에서 Member 엔티티를 추출합니다.
     * @param userDetails Spring Security UserDetails
     * @return Member 엔티티
     * @throws IllegalArgumentException 사용자를 찾을 수 없는 경우
     */
    public Member getMemberFromUserDetails(UserDetails userDetails) {
        if (userDetails == null) {
            throw new IllegalArgumentException("UserDetails가 null입니다.");
        }

        // 이메일로 Member 조회 (현재 방식 유지)
        return memberRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userDetails.getUsername()));
    }

    /**
     * 현재 인증된 사용자가 활성 상태인지 확인합니다.
     * @param userDetails Spring Security UserDetails
     * @return 사용자가 인증되고 활성 상태이면 true
     */
    public boolean isUserAuthenticatedAndActive(UserDetails userDetails) {
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

    @Transactional
    public TokenDto refreshAccessToken(HttpServletRequest request, HttpServletResponse response) {
        Optional<String> refreshTokenOpt = cookieUtil.getRefreshTokenFromCookies(request);

        if (refreshTokenOpt.isEmpty()) {
            throw new IllegalArgumentException("Refresh token not found");
        }

        String refreshToken = refreshTokenOpt.get();

        // Refresh Token 기본 검증
        if (!jwtUtil.isValidToken(refreshToken)) {
            cookieUtil.deleteAllAuthCookies(response);
            throw new IllegalArgumentException("Invalid refresh token format");
        }

        if (!jwtUtil.isRefreshToken(refreshToken)) {
            cookieUtil.deleteAllAuthCookies(response);
            throw new IllegalArgumentException("Wrong token type - not a refresh token");
        }

        if (jwtUtil.isTokenExpired(refreshToken)) {
            cookieUtil.deleteAllAuthCookies(response);
            throw new IllegalArgumentException("Refresh token expired");
        }

        try {
            TokenDto tokenDto = tokenService.refreshAccessToken(refreshToken);

            // 새로운 Access Token을 쿠키에 설정
            cookieUtil.addAccessTokenCookie(response, tokenDto.accessToken(),
                    Duration.ofMillis(tokenService.getJwtUtil().getAccessTokenExpirationTime()));

            log.info("Successfully refreshed access token for user ID: {}", jwtUtil.extractUserId(refreshToken));
            return tokenDto;

        } catch (IllegalArgumentException e) {
            // TokenService에서 발생한 예외 (Redis 검증 실패 등)
            cookieUtil.deleteAllAuthCookies(response);
            log.debug("Token refresh failed: {}", e.getMessage());
            throw new IllegalArgumentException("Refresh token validation failed");
        }
    }

    @Transactional
    public void logout(Member member, HttpServletRequest request, HttpServletResponse response) {
        String memberId = member.getMemberId().toString();
        Optional<String> accessToken = cookieUtil.getAccessTokenFromCookies(request);
        Optional<String> refreshToken = cookieUtil.getRefreshTokenFromCookies(request);

        // 토큰 무효화 (블랙리스트 추가 및 Redis에서 삭제)
        tokenService.logout(memberId, accessToken.orElse(null), refreshToken.orElse(null));

        // 쿠키 삭제
        cookieUtil.deleteAllAuthCookies(response);

        log.info("User logged out: {} (ID: {})", member.getEmail(), member.getMemberId());
    }

    @Transactional
    public Member createOrUpdateTestUser(String email, String name, Role role) {
        Member testMember = memberRepository.findByEmail(email)
                .orElseGet(() -> {
                    Member newMember = new Member("test-google-id-" + System.currentTimeMillis(),
                            email, name, "https://via.placeholder.com/150");
                    log.info("Created test user: {} with role: {}", email, role);
                    return memberRepository.save(newMember);
                });

        // 기존 사용자라면 정보 업데이트
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
            testMember = memberRepository.save(testMember);
            log.info("Updated test user: {} with role: {}", email, role);
        }

        return testMember;
    }

    public TokenDto generateTokensAndSetCookies(Member member, HttpServletResponse response) {
        TokenDto tokenDto = tokenService.generateTokens(member.getMemberId().toString(), member.getEmail());

        // HttpOnly 쿠키로 토큰 설정
        cookieUtil.addAccessTokenCookie(response, tokenDto.accessToken(),
                Duration.ofMillis(tokenService.getJwtUtil().getAccessTokenExpirationTime()));
        cookieUtil.addRefreshTokenCookie(response, tokenDto.refreshToken(),
                Duration.ofMillis(tokenService.getJwtUtil().getRefreshTokenExpirationTime()));

        log.debug("Generated tokens for user: {} (ID: {})", member.getEmail(), member.getMemberId());

        return tokenDto;
    }
}