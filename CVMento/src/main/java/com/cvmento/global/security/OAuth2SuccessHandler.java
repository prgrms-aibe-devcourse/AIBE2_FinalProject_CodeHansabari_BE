package com.cvmento.global.security;

import com.cvmento.domain.auth.dto.TokenDto;
import com.cvmento.global.common.util.CookieUtil;
import com.cvmento.domain.member.entity.Member;
import com.cvmento.domain.member.repository.MemberRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@Component
@Slf4j
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final TokenService tokenService;
    private final MemberRepository memberRepository;
    private final CookieUtil cookieUtil;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    public OAuth2SuccessHandler(TokenService tokenService,
                                MemberRepository memberRepository,
                                CookieUtil cookieUtil) {
        this.tokenService = tokenService;
        this.memberRepository = memberRepository;
        this.cookieUtil = cookieUtil;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        Map<String, Object> attributes = oAuth2User.getAttributes();

        String googleId = (String) attributes.get("sub");
        String email = (String) attributes.get("email");
        String name = (String) attributes.get("name");
        String picture = (String) attributes.get("picture");

        log.info("OAuth2 login attempt for user: {}", email);

        try {
            Member member = findOrCreateUser(googleId, email, name, picture);
            TokenDto tokenDto = tokenService.generateTokens(member.getMemberId().toString(), member.getEmail());

            cookieUtil.addAccessTokenCookie(response, tokenDto.accessToken(),
                    Duration.ofMillis(tokenService.getJwtUtil().getAccessTokenExpirationTime()));
            cookieUtil.addRefreshTokenCookie(response, tokenDto.refreshToken(),
                    Duration.ofMillis(tokenService.getJwtUtil().getRefreshTokenExpirationTime()));

            log.info("Successfully authenticated user: {} (ID: {})", email, member.getMemberId());

            String redirectUrl = frontendUrl + "/auth/callback?success=true";
            response.sendRedirect(redirectUrl);

        } catch (Exception e) {
            log.error("OAuth2 authentication failed for user: {}", email, e);
            response.sendRedirect(frontendUrl + "/auth/callback?error=true");
        }
    }

    private Member findOrCreateUser(String googleId, String email, String name, String picture) {
        Optional<Member> existingUser = memberRepository.findByGoogleId(googleId);

        if (existingUser.isPresent()) {
            Member member = existingUser.get();
            member.updateProfile(name, picture);
            member.updateLastLoginAt(LocalDateTime.now());

            if (!member.isActive()) {
                member.activate();
                log.info("Reactivated user: {}", email);
            }

            return memberRepository.save(member);
        } else {
            Member newUser = new Member(googleId, email, name, picture);
            newUser.updateLastLoginAt(LocalDateTime.now());

            Member savedUser = memberRepository.save(newUser);
            log.info("Created new user: {} (ID: {})", email, savedUser.getMemberId());

            return savedUser;
        }
    }
}