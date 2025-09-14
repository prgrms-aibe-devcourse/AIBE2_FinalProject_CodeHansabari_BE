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
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

/**
 * 인증 관련 API 컨트롤러
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Authentication", description = "인증 관련 API")
public class AuthController implements AuthControllerInterface {

    private final AuthService authService;
    private final GoogleOAuthService googleOAuthService;

    /**
     * 구글 OAuth2 로그인 URL 생성
     */
    @Override
    @GetMapping("/google/url")
    public ResponseEntity<CommonResponse<GoogleLoginUrlResponse>> getGoogleLoginUrl(
            @RequestParam(required = false) String redirectUri) {

        MDC.put("spanId", "google-url-controller");
        GoogleLoginUrlResponse response = googleOAuthService.generateGoogleLoginUrl(redirectUri);
        return ResponseEntity.ok(CommonResponse.success(response));
    }

    /**
     * 구글 OAuth2 로그인 처리
     */
    @Override
    @PostMapping("/google/login")
    public ResponseEntity<CommonResponse<LoginResponse>> loginWithGoogle(
            @Valid @RequestBody GoogleLoginRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) {

        MDC.put("spanId", "google-oauth-controller");

        try {
            LoginResponse loginResponse = googleOAuthService.processGoogleLogin(request, httpResponse);
            return ResponseEntity.ok(CommonResponse.success(loginResponse));

        } catch (InvalidAuthorizationCodeException e) {
            log.warn("구글 로그인 실패: 잘못된 authorization code");
            return ResponseEntity.badRequest()
                    .body(CommonResponse.error("INVALID_AUTH_CODE", e.getMessage()));

        } catch (GoogleApiException e) {
            log.error("구글 API 오류: {}", e.getMessage());
            return ResponseEntity.status(502)
                    .body(CommonResponse.error("GOOGLE_API_ERROR", "구글 서버와 통신 중 오류가 발생했습니다."));

        } catch (Exception e) {
            log.error("구글 로그인 처리 실패", e);
            return ResponseEntity.status(500)
                    .body(CommonResponse.error("LOGIN_ERROR", "로그인 처리 중 오류가 발생했습니다."));
        }
    }

    /**
     * 구글 ID 토큰 로그인 처리
     */
    @Override
    @PostMapping("/google/token")
    public ResponseEntity<CommonResponse<LoginResponse>> loginWithGoogleToken(
            @Valid @RequestBody GoogleTokenRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) {

        MDC.put("spanId", "google-token-controller");

        try {
            log.info("구글 토큰 로그인 시도");

            LoginResponse loginResponse = googleOAuthService.processGoogleTokenLogin(request, httpResponse);

            log.info("구글 토큰 로그인 성공 - memberId: {}", loginResponse.member().memberId());
            return ResponseEntity.ok(CommonResponse.success(loginResponse));

        } catch (InvalidTokenException e) {
            log.warn("구글 ID 토큰 검증 실패: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(CommonResponse.error("INVALID_TOKEN", e.getMessage()));

        } catch (Exception e) {
            log.error("구글 토큰 로그인 실패: {}", e.getMessage());
            return ResponseEntity.status(500)
                    .body(CommonResponse.error("LOGIN_ERROR", "로그인 처리 중 오류가 발생했습니다."));
        }
    }

    /**
     * 구글 로그인 안내
     */
    @Override
    @GetMapping("/login/google")
    public ResponseEntity<CommonResponse<GoogleLoginGuideResponse>> loginWithGoogle() {

        GoogleLoginGuideResponse response = new GoogleLoginGuideResponse(
                "구글 로그인을 시작하려면 아래 URL로 브라우저에서 직접 접속하세요.",
                "/oauth2/authorization/google",
                "http://localhost:8080/oauth2/authorization/google",
                "Swagger에서 API 테스트가 필요하면 /auth/test-login을 사용하세요."
        );

        return ResponseEntity.ok(CommonResponse.success(response));
    }

    /**
     * 토큰 갱신
     */
    @Override
    @PostMapping("/refresh")
    public ResponseEntity<CommonResponse<TokenRefreshResponse>> refreshToken(
            HttpServletRequest request,
            HttpServletResponse response) {

        MDC.put("spanId", "refresh-token-controller");

        try {
            authService.refreshAccessToken(request, response);
            TokenRefreshResponse refreshResponse = new TokenRefreshResponse("Token refreshed successfully");

            return ResponseEntity.ok(CommonResponse.success(refreshResponse));

        } catch (IllegalArgumentException e) {
            String message = e.getMessage();
            String errorCode;

            if (message != null && message.contains("not found")) {
                errorCode = "NO_REFRESH_TOKEN";
            } else if (message != null && message.contains("expired")) {
                errorCode = "REFRESH_TOKEN_EXPIRED";
            } else {
                errorCode = "INVALID_REFRESH_TOKEN";
            }

            return ResponseEntity.status(401)
                    .body(CommonResponse.error(errorCode, message, false));
        }
    }

    /**
     * 로그아웃
     */
    @Override
    @PostMapping("/logout")
    public ResponseEntity<CommonResponse<Void>> logout(
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest request,
            HttpServletResponse response) {

        MDC.put("spanId", "logout-controller");

        if (userDetails == null) {
            return ResponseEntity.status(401)
                    .body(CommonResponse.error("UNAUTHORIZED", "인증되지 않은 사용자입니다."));
        }

        try {
            Member member = authService.getMemberFromUserDetails(userDetails);
            log.info("로그아웃 요청 - memberId: {}", member.getMemberId());

            authService.logout(member, request, response);
            return ResponseEntity.ok(CommonResponse.success("로그아웃되었습니다."));
        } catch (IllegalArgumentException e) {
            log.debug("로그아웃 처리 중 오류: {}", e.getMessage());
            return ResponseEntity.status(401)
                    .body(CommonResponse.error("LOGOUT_FAILED", "로그아웃 처리에 실패했습니다."));
        }
    }

    /**
     * 현재 사용자 정보 조회
     */
    @Override
    @GetMapping("/me")
    public ResponseEntity<CommonResponse<?>> getCurrentUser(
            @AuthenticationPrincipal UserDetails userDetails) {

        MDC.put("spanId", "user-info-controller");

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

    /**
     * 인증 상태 확인
     */
    @Override
    @GetMapping("/status")
    public ResponseEntity<CommonResponse<AuthStatusResponse>> checkAuthStatus(
            @AuthenticationPrincipal UserDetails userDetails) {

        MDC.put("spanId", "auth-status-controller");

        if (authService.isUserAuthenticatedAndActive(userDetails)) {
            try {
                Member member = authService.getMemberFromUserDetails(userDetails);
                AuthStatusResponse statusResponse = new AuthStatusResponse(true, MemberInfo.from(member));
                return ResponseEntity.ok(CommonResponse.success(statusResponse));
            } catch (IllegalArgumentException e) {
                log.debug("인증 상태 확인 중 오류: {}", e.getMessage());
            }
        }

        AuthStatusResponse statusResponse = new AuthStatusResponse(false, null);
        return ResponseEntity.ok(CommonResponse.success(statusResponse));
    }

    /**
     * 개발용 테스트 로그인
     */

//    @PostMapping("/test-login")
//    @Operation(summary = "개발용 테스트 로그인", description = "개발/테스트용 임시 로그인입니다.")
//    @ApiResponse(responseCode = "200", description = "테스트 로그인 성공")
//    public ResponseEntity<CommonResponse<TestLoginResponse>> testLogin(
//            @RequestParam(defaultValue = "test@example.com") String email,
//            @RequestParam(defaultValue = "Test User") String name,
//            HttpServletResponse response) {
//
//        MDC.put("spanId", "test-login-controller");
//
//        Member testMember = authService.createOrUpdateTestUser(email, name, Role.USER);
//        authService.generateTokensAndSetCookies(testMember, response);
//
//        TestLoginResponse loginResponse = TestLoginResponse.of(
//                "테스트 로그인이 완료되었습니다.",
//                MemberInfo.from(testMember),
//                "쿠키가 자동으로 설정되었습니다."
//        );
//
//        return ResponseEntity.ok(CommonResponse.success(loginResponse));
//    }
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

    /**
     * 빠른 로그인 공통 처리
     */
    private ResponseEntity<CommonResponse<TestLoginResponse>> performQuickLogin(
            String email, String name, Role role, HttpServletResponse response) {

        Member testMember = authService.createOrUpdateTestUser(email, name, role);
        authService.generateTokensAndSetCookies(testMember, response);

        TestLoginResponse loginResponse = TestLoginResponse.of(
                name + "로 로그인되었습니다.",
                MemberInfo.from(testMember),
                null
        );

        return ResponseEntity.ok(CommonResponse.success(loginResponse));
    }
}
