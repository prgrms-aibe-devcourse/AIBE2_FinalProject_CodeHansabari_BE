package com.cvmento.domain.auth.controller;

import com.cvmento.domain.auth.dto.request.GoogleLoginRequest;
import com.cvmento.domain.auth.dto.request.GoogleTokenRequest;
import com.cvmento.domain.auth.dto.response.AuthStatusResponse;
import com.cvmento.domain.auth.dto.response.LoginResponse;
import com.cvmento.domain.auth.dto.response.GoogleLoginGuideResponse;
import com.cvmento.domain.auth.dto.response.GoogleLoginUrlResponse;
import com.cvmento.domain.auth.dto.response.TestLoginResponse;
import com.cvmento.domain.auth.dto.response.TokenRefreshResponse;
import com.cvmento.domain.auth.service.AuthService;
import com.cvmento.domain.auth.service.GoogleOAuthService;
import com.cvmento.domain.member.dto.MemberInfo;
import com.cvmento.domain.member.entity.Member;
import com.cvmento.domain.member.enums.Role;
import com.cvmento.global.common.dto.CommonResponse;
import com.cvmento.global.exception.customException.GoogleApiException;
import com.cvmento.global.exception.customException.InvalidAuthorizationCodeException;
import com.cvmento.global.exception.customException.InvalidTokenException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Authentication", description = "인증 관련 API")
public class AuthController {

    private final AuthService authService;
    private final GoogleOAuthService googleOAuthService;

    // === 구글 OAuth2 로그인 API ===

    @GetMapping("/google/url")
    @Operation(summary = "구글 OAuth2 URL 생성", description = "프론트엔드에서 사용할 구글 로그인 URL을 생성합니다.")
    @ApiResponse(responseCode = "200", description = "구글 로그인 URL 생성 성공")
    public ResponseEntity<CommonResponse<GoogleLoginUrlResponse>> getGoogleLoginUrl(
            @RequestParam(required = false) String redirectUri) {

        GoogleLoginUrlResponse response = googleOAuthService.generateGoogleLoginUrl(redirectUri);
        return ResponseEntity.ok(CommonResponse.success(response));
    }

    @PostMapping("/google/login")
    @Operation(summary = "구글 OAuth2 로그인", description = "구글에서 받은 authorization code로 로그인을 처리합니다.")
    @ApiResponse(responseCode = "200", description = "로그인 성공")
    @ApiResponse(responseCode = "400", description = "잘못된 authorization code")
    @ApiResponse(responseCode = "401", description = "인증 실패")
    public ResponseEntity<CommonResponse<LoginResponse>> loginWithGoogle(
            @Valid @RequestBody GoogleLoginRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) {

        try {
            String clientIp = getClientIpAddress(httpRequest);
            LoginResponse loginResponse = googleOAuthService.processGoogleLogin(request, clientIp, httpResponse);

            return ResponseEntity.ok(CommonResponse.success(loginResponse));

        } catch (InvalidAuthorizationCodeException e) {
            log.warn("Invalid authorization code: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(CommonResponse.error("INVALID_AUTH_CODE", e.getMessage()));

        } catch (GoogleApiException e) {
            log.error("Google API error: {}", e.getMessage());
            return ResponseEntity.status(502)
                    .body(CommonResponse.error("GOOGLE_API_ERROR", "구글 서버와 통신 중 오류가 발생했습니다."));

        } catch (Exception e) {
            log.error("Google login failed", e);
            return ResponseEntity.status(500)
                    .body(CommonResponse.error("LOGIN_ERROR", "로그인 처리 중 오류가 발생했습니다."));
        }
    }

    @PostMapping("/google/token")
    @Operation(
            summary = "구글 ID 토큰으로 로그인",
            description = "프론트엔드 Google Identity Services에서 받은 ID 토큰으로 로그인을 처리합니다."
    )
    @ApiResponse(responseCode = "200", description = "로그인 성공")
    @ApiResponse(responseCode = "400", description = "잘못된 ID 토큰")
    public ResponseEntity<CommonResponse<LoginResponse>> loginWithGoogleToken(
            @Valid @RequestBody GoogleTokenRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) {

        try {
            String clientIp = getClientIpAddress(httpRequest);
            log.info("구글 토큰 로그인 시도 - IP: {}", clientIp);

            LoginResponse loginResponse = googleOAuthService.processGoogleTokenLogin(request, clientIp, httpResponse);

            log.info("구글 토큰 로그인 성공 - 사용자: {}", loginResponse.getMember().email());
            return ResponseEntity.ok(CommonResponse.success(loginResponse));

        } catch (InvalidTokenException e) {
            log.warn("Invalid Google ID token: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(CommonResponse.error("INVALID_TOKEN", e.getMessage()));

        } catch (Exception e) {
            log.error("구글 토큰 로그인 실패 - IP: {}, 오류: {}", getClientIpAddress(httpRequest), e.getMessage());
            return ResponseEntity.status(500)
                    .body(CommonResponse.error("LOGIN_ERROR", "로그인 처리 중 오류가 발생했습니다."));
        }
    }

    @GetMapping("/login/google")
    @Operation(summary = "구글 로그인 안내", description = "구글 OAuth2 로그인 URL을 제공합니다.")
    @ApiResponse(responseCode = "200", description = "로그인 URL 정보 제공")
    public ResponseEntity<CommonResponse<GoogleLoginGuideResponse>> loginWithGoogle() {
        GoogleLoginGuideResponse response = GoogleLoginGuideResponse.builder()
                .message("구글 로그인을 시작하려면 아래 URL로 브라우저에서 직접 접속하세요.")
                .loginUrl("/oauth2/authorization/google")
                .fullUrl("http://localhost:8080/oauth2/authorization/google")
                .note("Swagger에서 API 테스트가 필요하면 /auth/test-login을 사용하세요.")
                .build();

        return ResponseEntity.ok(CommonResponse.success(response));
    }

    // === 토큰 관리 API ===

    @PostMapping("/refresh")
    @Operation(summary = "토큰 갱신", description = "Refresh Token으로 새로운 Access Token을 발급받습니다.")
    @ApiResponse(responseCode = "200", description = "토큰 갱신 성공")
    @ApiResponse(responseCode = "401", description = "유효하지 않은 Refresh Token")
    public ResponseEntity<CommonResponse<TokenRefreshResponse>> refreshToken(HttpServletRequest request, HttpServletResponse response) {
        try {
            authService.refreshAccessToken(request, response);
            TokenRefreshResponse refreshResponse = TokenRefreshResponse.builder()
                    .message("Token refreshed successfully")
                    .build();

            return ResponseEntity.ok(CommonResponse.success(refreshResponse));

        } catch (IllegalArgumentException e) {
            String message = e.getMessage();
            String errorCode;

            if (message.contains("not found")) {
                errorCode = "NO_REFRESH_TOKEN";
            } else if (message.contains("expired")) {
                errorCode = "REFRESH_TOKEN_EXPIRED";
            } else {
                errorCode = "INVALID_REFRESH_TOKEN";
            }

            return ResponseEntity.status(401)
                    .body(CommonResponse.error(errorCode, message, false));
        }
    }

    @PostMapping("/logout")
    @Operation(summary = "로그아웃", description = "로그아웃하고 모든 토큰을 무효화합니다.")
    @SecurityRequirement(name = "cookieAuth")
    @ApiResponse(responseCode = "200", description = "로그아웃 성공")
    public ResponseEntity<CommonResponse<Void>> logout(@AuthenticationPrincipal UserDetails userDetails,
                                                       HttpServletRequest request,
                                                       HttpServletResponse response) {
        if (userDetails == null) {
            return ResponseEntity.status(401)
                    .body(CommonResponse.error("UNAUTHORIZED", "인증되지 않은 사용자입니다."));
        }

        try {
            Member member = authService.getMemberFromUserDetails(userDetails);
            authService.logout(member, request, response);
            return ResponseEntity.ok(CommonResponse.success("로그아웃되었습니다."));
        } catch (IllegalArgumentException e) {
            log.debug("로그아웃 처리 중 오류: {}", e.getMessage());
            return ResponseEntity.status(401)
                    .body(CommonResponse.error("LOGOUT_FAILED", "로그아웃 처리에 실패했습니다."));
        }
    }

    // === 사용자 정보 API ===

    @GetMapping("/me")
    @Operation(summary = "현재 사용자 정보", description = "JWT 토큰으로 현재 로그인한 사용자 정보를 가져옵니다.")
    @SecurityRequirement(name = "cookieAuth")
    @ApiResponse(responseCode = "200", description = "사용자 정보 조회 성공")
    @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
    public ResponseEntity<CommonResponse<?>> getCurrentUser(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(401)
                    .body(CommonResponse.error("UNAUTHORIZED", "인증되지 않은 사용자입니다."));
        }

        try {
            Member member = authService.getMemberFromUserDetails(userDetails);
            return ResponseEntity.ok(CommonResponse.success(MemberInfo.from(member)));
        } catch (IllegalArgumentException e) {
            log.debug("사용자 조회 실패: {}", e.getMessage());
            return ResponseEntity.status(401)
                    .body(CommonResponse.error("USER_NOT_FOUND", "사용자를 찾을 수 없습니다."));
        }
    }

    @GetMapping("/status")
    @Operation(summary = "인증 상태 확인", description = "현재 인증 상태를 확인합니다.")
    public ResponseEntity<CommonResponse<AuthStatusResponse>> checkAuthStatus(@AuthenticationPrincipal UserDetails userDetails) {
        if (authService.isUserAuthenticatedAndActive(userDetails)) {
            try {
                Member member = authService.getMemberFromUserDetails(userDetails);
                AuthStatusResponse statusResponse = AuthStatusResponse.builder()
                        .authenticated(true)
                        .member(MemberInfo.from(member))
                        .build();
                return ResponseEntity.ok(CommonResponse.success(statusResponse));
            } catch (IllegalArgumentException e) {
                log.debug("인증 상태 확인 중 오류: {}", e.getMessage());
            }
        }

        // 인증되지 않았거나 활성 사용자가 아닌 경우
        AuthStatusResponse statusResponse = AuthStatusResponse.builder()
                .authenticated(false)
                .build();
        return ResponseEntity.ok(CommonResponse.success(statusResponse));
    }

    // === 개발용 테스트 엔드포인트들 ===

//     @PostMapping("/test-login")
//     @Operation(summary = "개발용 테스트 로그인", description = "개발/테스트용 임시 로그인입니다.")
//     @ApiResponse(responseCode = "200", description = "테스트 로그인 성공")
//     public ResponseEntity<CommonResponse<TestLoginResponse>> testLogin(
//             @RequestParam(defaultValue = "test@example.com") String email,
//             @RequestParam(defaultValue = "Test User") String name,
//             HttpServletResponse response) {
//
//         Member testMember = authService.createOrUpdateTestUser(email, name, Role.USER);
//         authService.generateTokensAndSetCookies(testMember, response);
//
//         TestLoginResponse loginResponse = TestLoginResponse.of(
//                 "테스트 로그인이 완료되었습니다.",
//                 MemberInfo.from(testMember),
//                 "쿠키가 자동으로 설정되었습니다."
//         );
//
//         return ResponseEntity.ok(CommonResponse.success(loginResponse));
//     }
//
//     @PostMapping("/quick-login/user")
//     @Operation(summary = "일반 사용자로 빠른 로그인")
//     @ApiResponse(responseCode = "200", description = "테스트 로그인 성공")
//     public ResponseEntity<CommonResponse<TestLoginResponse>> quickLoginAsUser(HttpServletResponse response) {
//         return performQuickLogin("user@test.com", "일반 사용자", Role.USER, response);
//     }
//
//     @PostMapping("/quick-login/expert")
//     @Operation(summary = "최상위 관리자로 빠른 로그인")
//     @ApiResponse(responseCode = "200", description = "테스트 로그인 성공")
//     public ResponseEntity<CommonResponse<TestLoginResponse>> quickLoginAsExpert(HttpServletResponse response) {
//         return performQuickLogin("root@test.com", "최상위 관리자", Role.ROOT, response);
//     }
//
//     @PostMapping("/quick-login/admin")
//     @Operation(summary = "관리자로 빠른 로그인")
//     @ApiResponse(responseCode = "200", description = "테스트 로그인 성공")
//     public ResponseEntity<CommonResponse<TestLoginResponse>> quickLoginAsAdmin(HttpServletResponse response) {
//         return performQuickLogin("admin@test.com", "관리자", Role.ADMIN, response);
//     }

    // === 헬퍼 메서드들 ===

    private ResponseEntity<CommonResponse<TestLoginResponse>> performQuickLogin(String email, String name, Role role, HttpServletResponse response) {
        Member testMember = authService.createOrUpdateTestUser(email, name, role);
        authService.generateTokensAndSetCookies(testMember, response);

        TestLoginResponse loginResponse = TestLoginResponse.of(
                name + "로 로그인되었습니다.",
                MemberInfo.from(testMember),
                null
        );

        return ResponseEntity.ok(CommonResponse.success(loginResponse));
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
 
        return request.getRemoteAddr();
    }
}
