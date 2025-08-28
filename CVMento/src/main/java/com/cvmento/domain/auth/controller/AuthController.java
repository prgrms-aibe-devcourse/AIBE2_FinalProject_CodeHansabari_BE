package com.cvmento.domain.auth.controller;

import com.cvmento.domain.auth.dto.response.AuthStatusResponse;
import com.cvmento.domain.auth.dto.response.GoogleLoginGuideResponse;
import com.cvmento.domain.auth.dto.response.TestLoginResponse;
import com.cvmento.domain.auth.dto.response.TokenRefreshResponse;
import com.cvmento.domain.auth.service.AuthService;
import com.cvmento.domain.member.dto.MemberInfo;
import com.cvmento.domain.member.entity.Member;
import com.cvmento.domain.member.enums.Role;
import com.cvmento.global.common.dto.CommonResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "인증 관련 API")
public class AuthController {

    private final AuthService authService;

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

    @GetMapping("/me")
    @Operation(summary = "현재 사용자 정보", description = "쿠키의 JWT 토큰으로 현재 로그인한 사용자 정보를 가져옵니다.")
    @SecurityRequirement(name = "cookieAuth")
    @ApiResponse(responseCode = "200", description = "사용자 정보 조회 성공")
    @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
    public ResponseEntity<CommonResponse<?>> getCurrentUser(@AuthenticationPrincipal Member member) {
        if (member == null) {
            return ResponseEntity.status(401)
                    .body(CommonResponse.error("UNAUTHORIZED", "인증되지 않은 사용자입니다."));
        }

        return ResponseEntity.ok(CommonResponse.success(MemberInfo.from(member)));
    }

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
    public ResponseEntity<CommonResponse<Void>> logout(@AuthenticationPrincipal Member member,
                                                       HttpServletRequest request,
                                                       HttpServletResponse response) {
        authService.logout(member, request, response);
        return ResponseEntity.ok(CommonResponse.success("로그아웃되었습니다."));
    }

    @GetMapping("/status")
    @Operation(summary = "인증 상태 확인", description = "현재 인증 상태를 확인합니다.")
    public ResponseEntity<CommonResponse<AuthStatusResponse>> checkAuthStatus(@AuthenticationPrincipal Member member) {
        if (member != null && member.isActive()) {
            AuthStatusResponse statusResponse = AuthStatusResponse.builder()
                    .authenticated(true)
                    .member(MemberInfo.from(member))
                    .build();
            return ResponseEntity.ok(CommonResponse.success(statusResponse));
        } else {
            AuthStatusResponse statusResponse = AuthStatusResponse.builder()
                    .authenticated(false)
                    .build();
            return ResponseEntity.ok(CommonResponse.success(statusResponse));
        }
    }

    // === 개발용 테스트 엔드포인트들 ===

    @PostMapping("/test-login")
    @Operation(summary = "개발용 테스트 로그인", description = "개발/테스트용 임시 로그인입니다.")
    @ApiResponse(responseCode = "200", description = "테스트 로그인 성공")
    public ResponseEntity<CommonResponse<TestLoginResponse>> testLogin(
            @RequestParam(defaultValue = "test@example.com") String email,
            @RequestParam(defaultValue = "Test User") String name,
            HttpServletResponse response) {

        Member testMember = authService.createOrUpdateTestUser(email, name, Role.USER);
        authService.generateTokensAndSetCookies(testMember, response);

        TestLoginResponse loginResponse = TestLoginResponse.of(
                "테스트 로그인이 완료되었습니다.",
                MemberInfo.from(testMember),
                "쿠키가 자동으로 설정되었습니다."
        );

        return ResponseEntity.ok(CommonResponse.success(loginResponse));
    }

    @PostMapping("/quick-login/user")
    @Operation(summary = "일반 사용자로 빠른 로그인")
    @ApiResponse(responseCode = "200", description = "테스트 로그인 성공")
    public ResponseEntity<CommonResponse<TestLoginResponse>> quickLoginAsUser(HttpServletResponse response) {
        return performQuickLogin("user@test.com", "일반 사용자", Role.USER, response);
    }

    @PostMapping("/quick-login/expert")
    @Operation(summary = "최상위 관리자로 빠른 로그인")
    @ApiResponse(responseCode = "200", description = "테스트 로그인 성공")
    public ResponseEntity<CommonResponse<TestLoginResponse>> quickLoginAsExpert(HttpServletResponse response) {
        return performQuickLogin("root@test.com", "최상위 관리자", Role.ROOT, response);
    }

    @PostMapping("/quick-login/admin")
    @Operation(summary = "관리자로 빠른 로그인")
    @ApiResponse(responseCode = "200", description = "테스트 로그인 성공")
    public ResponseEntity<CommonResponse<TestLoginResponse>> quickLoginAsAdmin(HttpServletResponse response) {
        return performQuickLogin("admin@test.com", "관리자", Role.ADMIN, response);
    }

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
}