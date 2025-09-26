package com.cvmento.domain.auth.service;

import com.cvmento.domain.auth.dto.request.GoogleLoginRequest;
import com.cvmento.domain.auth.dto.request.GoogleTokenRequest;
import com.cvmento.domain.auth.dto.response.GoogleLoginUrlResponse;
import com.cvmento.domain.auth.dto.response.LoginResponse;
import com.cvmento.domain.auth.dto.TokenDto;
import com.cvmento.domain.member.dto.MemberInfo;
import com.cvmento.domain.member.entity.Member;
import com.cvmento.domain.member.repository.MemberRepository;
import com.cvmento.global.common.MetricsService;
import com.cvmento.global.common.util.CookieUtil;
import com.cvmento.global.exception.customException.GoogleApiException;
import com.cvmento.global.exception.customException.InvalidAuthorizationCodeException;
import com.cvmento.global.exception.customException.InvalidTokenException;
import com.cvmento.global.security.TokenService;
import com.cvmento.global.usage.service.NewUserTokenInitializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class GoogleOAuthService {

    private final MemberRepository memberRepository;
    private final TokenService tokenService;
    private final CookieUtil cookieUtil;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final NewUserTokenInitializer newUserTokenInitializer;
    private final MetricsService metricsService;

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String googleClientId;

    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    private String googleClientSecret;

    @Value("${spring.security.oauth2.client.registration.google.redirect-uri:http://localhost:8080/login/oauth2/code/google}")
    private String defaultRedirectUri;

    private static final String GOOGLE_AUTH_URL = "https://accounts.google.com/o/oauth2/v2/auth";
    private static final String GOOGLE_TOKEN_URL = "https://oauth2.googleapis.com/token";
    private static final String GOOGLE_USER_INFO_URL = "https://www.googleapis.com/oauth2/v3/userinfo";
    private static final String GOOGLE_TOKEN_INFO_URL = "https://oauth2.googleapis.com/tokeninfo";

    /**
     * 구글 OAuth2 로그인 URL 생성
     */
    public GoogleLoginUrlResponse generateGoogleLoginUrl(String customRedirectUri) {
        MDC.put("spanId", "google-url-service");

        String state = UUID.randomUUID().toString();
        String redirectUri = StringUtils.hasText(customRedirectUri) ? customRedirectUri : defaultRedirectUri;

        String loginUrl = UriComponentsBuilder.fromUriString(GOOGLE_AUTH_URL)
                .queryParam("client_id", googleClientId)
                .queryParam("redirect_uri", redirectUri)
                .queryParam("scope", "openid profile email")
                .queryParam("response_type", "code")
                .queryParam("state", state)
                .queryParam("access_type", "offline")
                .queryParam("prompt", "consent")
                .build(true)
                .toUriString();

        log.info("구글 OAuth URL 생성 완료");
        return new GoogleLoginUrlResponse(loginUrl, state);
    }

    /**
     * Authorization Code를 사용한 구글 로그인 처리
     */
    @Transactional
    public LoginResponse processGoogleLogin(GoogleLoginRequest request, HttpServletResponse response) {
        MDC.put("spanId", "google-oauth-service");

        try {
            MDC.put("spanId", "google-token-api");
            GoogleTokenResponse tokenResponse = exchangeCodeForToken(request.code(), request.redirectUri());

            MDC.put("spanId", "google-userinfo-api");
            GoogleUserInfo userInfo = getUserInfoFromGoogle(tokenResponse.getAccessToken());

            MDC.put("spanId", "google-oauth-service");
            Member member = findOrCreateMember(userInfo);

            MDC.put("spanId", "token-service");
            TokenDto tokenDto = tokenService.generateTokens(member.getMemberId().toString(), member.getEmail());

            MDC.put("spanId", "google-oauth-service");
            setAuthenticationCookies(response, tokenDto);

            log.info("구글 OAuth 로그인 성공 - memberId: {}", member.getMemberId());

            metricsService.incrementLoginCount();

            return new LoginResponse(
                    "구글 로그인이 완료되었습니다.",
                    MemberInfo.from(member),
                    "로그인 상태가 쿠키에 저장되었습니다."
            );

        } catch (GoogleApiException | InvalidAuthorizationCodeException e) {
            metricsService.incrementErrorCount("GOOGLE_OAUTH_LOGIN_FAILED");
            throw e;
        } catch (Exception e) {
            log.error("구글 OAuth 로그인 실패", e);
            metricsService.incrementErrorCount("GOOGLE_OAUTH_LOGIN_FAILED");
            throw new GoogleApiException("구글 로그인 처리 중 오류가 발생했습니다.", e);
        }
    }

    /**
     * Google ID Token을 사용한 로그인 처리 (프론트엔드 SDK용) - 보안 강화
     */
    @Transactional
    public LoginResponse processGoogleTokenLogin(GoogleTokenRequest request, HttpServletResponse response) {
        MDC.put("spanId", "google-token-service");

        try {
            MDC.put("spanId", "google-token-verify-api");
            GoogleUserInfo userInfo = verifyGoogleIdToken(request.idToken());

            MDC.put("spanId", "google-token-service");
            Member member = findOrCreateMember(userInfo);

            MDC.put("spanId", "token-service");
            TokenDto tokenDto = tokenService.generateTokens(member.getMemberId().toString(), member.getEmail());

            MDC.put("spanId", "google-token-service");
            setAuthenticationCookies(response, tokenDto);

            log.info("구글 토큰 로그인 성공 - memberId: {}", member.getMemberId());

            metricsService.incrementLoginCount();

            return new LoginResponse(
                    "구글 로그인이 완료되었습니다.",
                    MemberInfo.from(member),
                    "로그인 상태가 쿠키에 저장되었습니다."
            );

        } catch (InvalidTokenException e) {
            metricsService.incrementErrorCount("GOOGLE_TOKEN_LOGIN_FAILED");
            throw e;
        } catch (Exception e) {
            log.error("구글 토큰 로그인 실패", e);
            metricsService.incrementErrorCount("GOOGLE_TOKEN_LOGIN_FAILED");
            throw new InvalidTokenException("Google ID Token 검증에 실패했습니다.");
        }
    }


    private GoogleTokenResponse exchangeCodeForToken(String code, String redirectUri) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("client_id", googleClientId);
        params.add("client_secret", googleClientSecret);
        params.add("code", code);
        params.add("grant_type", "authorization_code");
        params.add("redirect_uri", redirectUri != null ? redirectUri : defaultRedirectUri);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(params, headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    GOOGLE_TOKEN_URL, HttpMethod.POST, entity, String.class);

            if (response.getStatusCode() != HttpStatus.OK) {
                metricsService.incrementErrorCount("GOOGLE_TOKEN_EXCHANGE_FAILED");
                throw new InvalidAuthorizationCodeException("구글 토큰 교환에 실패했습니다.");
            }

            JsonNode jsonNode = objectMapper.readTree(response.getBody());

            if (jsonNode.has("error")) {
                String error = jsonNode.get("error").asText();
                metricsService.incrementErrorCount("GOOGLE_TOKEN_EXCHANGE_ERROR");
                throw new InvalidAuthorizationCodeException("구글 토큰 교환 오류: " + error);
            }

            return new GoogleTokenResponse(
                    jsonNode.get("access_token").asText(),
                    jsonNode.has("id_token") ? jsonNode.get("id_token").asText() : null,
                    jsonNode.get("expires_in").asInt()
            );

        } catch (InvalidAuthorizationCodeException e) {
            throw e;
        } catch (Exception e) {
            metricsService.incrementErrorCount("GOOGLE_API_COMMUNICATION_ERROR");
            throw new GoogleApiException("구글 API 통신 중 오류가 발생했습니다.", e);
        }
    }

    private GoogleUserInfo getUserInfoFromGoogle(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    GOOGLE_USER_INFO_URL, HttpMethod.GET, entity, String.class);

            if (response.getStatusCode() != HttpStatus.OK) {
                metricsService.incrementErrorCount("GOOGLE_USER_INFO_FAILED");
                throw new GoogleApiException("구글 사용자 정보 조회에 실패했습니다.");
            }

            JsonNode jsonNode = objectMapper.readTree(response.getBody());
            return parseGoogleUserInfo(jsonNode);

        } catch (GoogleApiException e) {
            throw e;
        } catch (Exception e) {
            metricsService.incrementErrorCount("GOOGLE_USER_INFO_ERROR");
            throw new GoogleApiException("구글 사용자 정보 조회 중 오류가 발생했습니다.", e);
        }
    }

    /**
     * Google의 tokeninfo API를 사용한 안전한 ID Token 검증
     */
    private GoogleUserInfo verifyGoogleIdToken(String idToken) {
        try {
            // Google의 tokeninfo API로 검증
            String verifyUrl = GOOGLE_TOKEN_INFO_URL + "?id_token=" + idToken;

            ResponseEntity<String> response = restTemplate.getForEntity(verifyUrl, String.class);

            if (response.getStatusCode() != HttpStatus.OK) {
                metricsService.incrementErrorCount("GOOGLE_TOKEN_VERIFY_FAILED");
                throw new InvalidTokenException("Google 토큰 검증에 실패했습니다.");
            }

            JsonNode jsonNode = objectMapper.readTree(response.getBody());

            // 에러 체크
            if (jsonNode.has("error")) {
                String error = jsonNode.get("error").asText();
                metricsService.incrementErrorCount("GOOGLE_TOKEN_INVALID");
                throw new InvalidTokenException("유효하지 않은 토큰입니다: " + error);
            }

            // audience(클라이언트 ID) 검증
            String audience = jsonNode.get("aud").asText();
            if (!googleClientId.equals(audience)) {
                metricsService.incrementErrorCount("GOOGLE_TOKEN_INVALID_AUDIENCE");
                throw new InvalidTokenException("잘못된 클라이언트 ID입니다.");
            }

            // 토큰이 아직 유효한지 확인 (Google이 이미 검증해주지만 추가 확인)
            if (!jsonNode.has("email_verified") || !jsonNode.get("email_verified").asBoolean()) {
                metricsService.incrementErrorCount("GOOGLE_EMAIL_NOT_VERIFIED");
                throw new InvalidTokenException("이메일이 인증되지 않은 구글 계정입니다.");
            }

            log.info("Google ID Token successfully verified for user: {}", jsonNode.get("email").asText());

            return parseGoogleUserInfo(jsonNode);

        } catch (InvalidTokenException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error during Google ID Token verification", e);
            metricsService.incrementErrorCount("GOOGLE_TOKEN_VERIFY_ERROR");
            throw new InvalidTokenException("ID Token 검증 중 오류가 발생했습니다.");
        }
    }

    private GoogleUserInfo parseGoogleUserInfo(JsonNode jsonNode) {
        String googleId = jsonNode.get("sub").asText();
        String email = jsonNode.get("email").asText();
        String name = jsonNode.has("name") ? jsonNode.get("name").asText() : email.split("@")[0];
        String picture = jsonNode.has("picture") ? jsonNode.get("picture").asText() : null;

        return new GoogleUserInfo(googleId, email, name, picture);
    }

    private Member findOrCreateMember(GoogleUserInfo userInfo) {
        MDC.put("spanId", "member-repository");
        Optional<Member> existingMember = memberRepository.findByGoogleId(userInfo.getGoogleId());

        if (existingMember.isPresent()) {
            Member member = existingMember.get();

            // 프로필 정보 업데이트 (null 안전성 개선)
            boolean needsUpdate = false;
            String currentName = member.getName();
            String newName = userInfo.getName();
            String currentPicture = member.getPicture();
            String newPicture = userInfo.getPicture();

            if ((currentName == null && newName != null) ||
                    (currentName != null && !currentName.equals(newName)) ||
                    (currentPicture == null && newPicture != null) ||
                    (currentPicture != null && !currentPicture.equals(newPicture))) {

                member.updateProfile(newName, newPicture);
                needsUpdate = true;
            }

            member.updateLastLoginAt(LocalDateTime.now());

            if (!member.isActive()) {
                member.activate();
                needsUpdate = true;
            }

            if (needsUpdate) {
                member = memberRepository.save(member);
            }

            return member;
        } else {
            // 새 사용자 생성
            Member newMember = new Member(userInfo.getGoogleId(), userInfo.getEmail(),
                    userInfo.getName(), userInfo.getPicture());
            newMember.updateLastLoginAt(LocalDateTime.now());
            Member savedMember = memberRepository.save(newMember);

            // 🔥 신규 사용자 토큰 초기화 (추가된 부분)
            try {
                newUserTokenInitializer.initializeNewUserTokens(savedMember.getMemberId());
                log.info("신규 가입 사용자 토큰 초기화 완료 - memberId: {}", savedMember.getMemberId());
            } catch (Exception e) {
                log.error("신규 사용자 토큰 초기화 실패 - memberId: {}, 오류: {}",
                        savedMember.getMemberId(), e.getMessage());
                metricsService.incrementErrorCount("NEW_USER_TOKEN_INIT_FAILED");
            }

            return savedMember;
        }
    }

    private void setAuthenticationCookies(HttpServletResponse response, TokenDto tokenDto) {
        cookieUtil.addAccessTokenCookie(response, tokenDto.accessToken(),
                Duration.ofMillis(tokenService.getJwtUtil().getAccessTokenExpirationTime()));
        cookieUtil.addRefreshTokenCookie(response, tokenDto.refreshToken(),
                Duration.ofMillis(tokenService.getJwtUtil().getRefreshTokenExpirationTime()));
    }

    // DTO 클래스들
    public static class GoogleTokenResponse {
        private final String accessToken;
        private final String idToken;
        private final int expiresIn;

        public GoogleTokenResponse(String accessToken, String idToken, int expiresIn) {
            this.accessToken = accessToken;
            this.idToken = idToken;
            this.expiresIn = expiresIn;
        }

        public String getAccessToken() { return accessToken; }
        public String getIdToken() { return idToken; }
        public int getExpiresIn() { return expiresIn; }
    }

    public static class GoogleUserInfo {
        private final String googleId;
        private final String email;
        private final String name;
        private final String picture;

        public GoogleUserInfo(String googleId, String email, String name, String picture) {
            this.googleId = googleId;
            this.email = email;
            this.name = name;
            this.picture = picture;
        }

        public String getGoogleId() { return googleId; }
        public String getEmail() { return email; }
        public String getName() { return name; }
        public String getPicture() { return picture; }
    }
}