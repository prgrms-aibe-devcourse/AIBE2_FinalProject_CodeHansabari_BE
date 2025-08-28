package com.cvmento.global.security;

import com.cvmento.global.common.util.CookieUtil;
import com.cvmento.domain.member.entity.Member;
import com.cvmento.domain.member.repository.MemberRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

@Component
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final TokenService tokenService;
    private final MemberRepository memberRepository;
    private final CookieUtil cookieUtil;

    public JwtAuthenticationFilter(JwtUtil jwtUtil,
                                   TokenService tokenService,
                                   MemberRepository memberRepository,
                                   CookieUtil cookieUtil) {
        this.jwtUtil = jwtUtil;
        this.tokenService = tokenService;
        this.memberRepository = memberRepository;
        this.cookieUtil = cookieUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // 쿠키에서 Access Token 추출
        Optional<String> tokenOpt = cookieUtil.getAccessTokenFromCookies(request);

        if (tokenOpt.isEmpty()) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = tokenOpt.get();

        try {
            // 1. 토큰이 블랙리스트에 있는지 확인
            if (tokenService.isTokenBlacklisted(token)) {
                log.debug("Token is blacklisted");
                setUnauthorizedResponse(response, "TOKEN_BLACKLISTED", "로그아웃된 토큰입니다.", false);
                return;
            }

            // 2. 토큰 기본 형식 검증
            if (!jwtUtil.isValidToken(token)) {
                log.debug("Invalid token format");
                cookieUtil.deleteAllAuthCookies(response);
                setUnauthorizedResponse(response, "INVALID_TOKEN", "유효하지 않은 토큰 형식입니다.", false);
                return;
            }

            // 3. Access Token 타입 확인
            if (!jwtUtil.isAccessToken(token)) {
                log.debug("Not an access token");
                setUnauthorizedResponse(response, "WRONG_TOKEN_TYPE", "Access Token이 아닙니다.", false);
                return;
            }

            // 4. 토큰 만료 확인
            if (jwtUtil.isTokenExpired(token)) {
                log.debug("Access token expired");
                handleExpiredAccessToken(request, response);
                return;
            }

            // 5. 사용자 정보 확인
            String userId = jwtUtil.extractUserId(token);
            Optional<Member> memberOpt = memberRepository.findById(Long.parseLong(userId));

            if (memberOpt.isEmpty()) {
                log.debug("User not found for ID: {}", userId);
                cookieUtil.deleteAllAuthCookies(response);
                setUnauthorizedResponse(response, "USER_NOT_FOUND", "사용자를 찾을 수 없습니다.", false);
                return;
            }

            Member member = memberOpt.get();

            // 6. 사용자 활성 상태 확인
            if (!member.isActive()) {
                log.debug("User is not active: {}", userId);
                setUnauthorizedResponse(response, "USER_DEACTIVATED", "비활성화된 계정입니다.", false);
                return;
            }

            // 7. 사용자 세션 유효성 검증
            if (!tokenService.isUserSessionValid(userId)) {
                log.debug("User session is not valid: {}", userId);
                setUnauthorizedResponse(response, "SESSION_INVALID", "세션이 유효하지 않습니다.", false);
                return;
            }

            // 8. 정상 인증 처리
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            member,
                            null,
                            member.getRole().getAuthorities()
                    );

            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);

            log.debug("Successfully authenticated user: {}", userId);

        } catch (Exception e) {
            log.error("JWT authentication failed", e);
            SecurityContextHolder.clearContext();
            setUnauthorizedResponse(response, "AUTHENTICATION_ERROR", "인증 처리 중 오류가 발생했습니다.", false);
            return;
        }

        filterChain.doFilter(request, response);
    }

    private void handleExpiredAccessToken(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // Refresh Token 존재 여부 확인
        Optional<String> refreshTokenOpt = cookieUtil.getRefreshTokenFromCookies(request);

        if (refreshTokenOpt.isEmpty()) {
            cookieUtil.deleteAllAuthCookies(response);
            setUnauthorizedResponse(response, "NO_REFRESH_TOKEN", "세션이 만료되었습니다. 다시 로그인해주세요.", false);
            return;
        }

        String refreshToken = refreshTokenOpt.get();

        // Refresh Token 유효성 검증
        if (!jwtUtil.isValidToken(refreshToken) || !jwtUtil.isRefreshToken(refreshToken)) {
            log.debug("Invalid refresh token");
            cookieUtil.deleteAllAuthCookies(response);
            setUnauthorizedResponse(response, "INVALID_REFRESH_TOKEN", "세션이 만료되었습니다. 다시 로그인해주세요.", false);
            return;
        }

        if (jwtUtil.isTokenExpired(refreshToken)) {
            log.debug("Refresh token expired");
            cookieUtil.deleteAllAuthCookies(response);
            setUnauthorizedResponse(response, "REFRESH_TOKEN_EXPIRED", "세션이 만료되었습니다. 다시 로그인해주세요.", false);
            return;
        }

        // Refresh Token이 유효한 경우 - Access Token 갱신 필요
        String userId = jwtUtil.extractUserId(refreshToken);
        if (!tokenService.isUserSessionValid(userId)) {
            log.debug("User session is not valid for refresh");
            cookieUtil.deleteAllAuthCookies(response);
            setUnauthorizedResponse(response, "SESSION_EXPIRED", "세션이 만료되었습니다. 다시 로그인해주세요.", false);
            return;
        }

        // Access Token만 만료되고 Refresh Token은 유효한 경우
        setUnauthorizedResponse(response, "ACCESS_TOKEN_EXPIRED", "Access Token이 만료되었습니다.", true);
    }

    private void setUnauthorizedResponse(HttpServletResponse response, String errorCode, String message, boolean canRetry) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        response.setCharacterEncoding("UTF-8");

        String jsonResponse = String.format(
                "{\"success\":false,\"errorCode\":\"%s\",\"message\":\"%s\",\"canRetry\":%b,\"timestamp\":%d}",
                errorCode, message, canRetry, System.currentTimeMillis()
        );

        response.getWriter().write(jsonResponse);
    }
}