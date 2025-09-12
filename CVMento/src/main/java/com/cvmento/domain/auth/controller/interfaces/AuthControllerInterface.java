package com.cvmento.domain.auth.controller.interfaces;

import com.cvmento.domain.auth.dto.request.GoogleLoginRequest;
import com.cvmento.domain.auth.dto.request.GoogleTokenRequest;
import com.cvmento.domain.auth.dto.response.AuthStatusResponse;
import com.cvmento.domain.auth.dto.response.GoogleLoginGuideResponse;
import com.cvmento.domain.auth.dto.response.GoogleLoginUrlResponse;
import com.cvmento.domain.auth.dto.response.LoginResponse;
import com.cvmento.domain.auth.dto.response.TokenRefreshResponse;
import com.cvmento.global.common.dto.CommonResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "Authentication", description = "인증 관련 API - 구글 OAuth2 로그인, 토큰 관리, 사용자 정보 조회")
public interface AuthControllerInterface {

    @Operation(
            summary = "구글 OAuth2 URL 생성",
            description = "프론트엔드에서 사용할 구글 로그인 URL을 생성합니다. 생성된 URL로 리다이렉션하면 구글 OAuth2 인증 과정이 시작됩니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "구글 로그인 URL 생성 성공",
                            content = @Content(
                                    schema = @Schema(implementation = CommonResponse.class),
                                    examples = @ExampleObject(
                                            name = "구글 OAuth URL 응답",
                                            value = """
                                                    {
                                                      "success": true,
                                                      "message": "요청이 성공적으로 처리되었습니다.",
                                                      "data": {
                                                        "loginUrl": "https://accounts.google.com/o/oauth2/v2/auth?client_id=...&redirect_uri=...&scope=openid%20profile%20email&response_type=code&state=...",
                                                        "state": "550e8400-e29b-41d4-a716-446655440000"
                                                      },
                                                      "timestamp": "2024-01-15T14:30:00"
                                                    }
                                                    """
                                    )
                            )
                    )
            }
    )
    ResponseEntity<CommonResponse<GoogleLoginUrlResponse>> getGoogleLoginUrl(
            @RequestParam(required = false) String redirectUri
    );

    @Operation(
            summary = "구글 OAuth2 로그인",
            description = "구글에서 받은 authorization code로 로그인을 처리합니다. 성공 시 JWT 토큰이 쿠키로 설정됩니다.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "구글 OAuth2 인증 후 받은 authorization code와 관련 정보",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = GoogleLoginRequest.class),
                            examples = @ExampleObject(
                                    name = "구글 OAuth 로그인 요청",
                                    value = """
                                            {
                                              "code": "4/0AfgeXvs...",
                                              "state": "550e8400-e29b-41d4-a716-446655440000",
                                              "redirectUri": "http://localhost:3000/auth/callback"
                                            }
                                            """
                            )
                    )
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "로그인 성공",
                            content = @Content(
                                    schema = @Schema(implementation = CommonResponse.class),
                                    examples = @ExampleObject(
                                            name = "로그인 성공 응답",
                                            value = """
                                                    {
                                                      "success": true,
                                                      "message": "요청이 성공적으로 처리되었습니다.",
                                                      "data": {
                                                        "message": "구글 로그인이 완료되었습니다.",
                                                        "member": {
                                                          "memberId": 123,
                                                          "email": "user@gmail.com",
                                                          "name": "김철수",
                                                          "picture": "https://lh3.googleusercontent.com/...",
                                                          "role": "USER",
                                                          "isActive": true,
                                                          "createdAt": "2024-01-15T10:30:00",
                                                          "lastLoginAt": "2024-01-15T14:25:30"
                                                        },
                                                        "note": "로그인 상태가 쿠키에 저장되었습니다."
                                                      },
                                                      "timestamp": "2024-01-15T14:30:00"
                                                    }
                                                    """
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "잘못된 authorization code",
                            content = @Content(
                                    schema = @Schema(implementation = CommonResponse.class),
                                    examples = @ExampleObject(
                                            name = "인증 코드 오류",
                                            value = """
                                                    {
                                                      "success": false,
                                                      "errorCode": "INVALID_AUTH_CODE",
                                                      "message": "유효하지 않은 인증 코드입니다.",
                                                      "timestamp": "2024-01-15T14:30:00"
                                                    }
                                                    """
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "502",
                            description = "구글 API 통신 오류",
                            content = @Content(
                                    schema = @Schema(implementation = CommonResponse.class),
                                    examples = @ExampleObject(
                                            name = "구글 API 오류",
                                            value = """
                                                    {
                                                      "success": false,
                                                      "errorCode": "GOOGLE_API_ERROR",
                                                      "message": "구글 서버와 통신 중 오류가 발생했습니다.",
                                                      "timestamp": "2024-01-15T14:30:00"
                                                    }
                                                    """
                                    )
                            )
                    )
            }
    )
    ResponseEntity<CommonResponse<LoginResponse>> loginWithGoogle(
            @Valid @RequestBody GoogleLoginRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse
    );

    @Operation(
            summary = "구글 ID 토큰으로 로그인",
            description = "프론트엔드 Google Identity Services에서 받은 ID 토큰으로 로그인을 처리합니다.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Google Identity Services에서 받은 ID 토큰",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = GoogleTokenRequest.class),
                            examples = @ExampleObject(
                                    name = "구글 ID 토큰 로그인 요청",
                                    value = """
                                            {
                                              "idToken": "eyJhbGciOiJSUzI1NiIsImtpZCI6..."
                                            }
                                            """
                            )
                    )
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "로그인 성공",
                            content = @Content(
                                    schema = @Schema(implementation = CommonResponse.class),
                                    examples = @ExampleObject(
                                            name = "ID 토큰 로그인 성공",
                                            value = """
                                                    {
                                                      "success": true,
                                                      "message": "요청이 성공적으로 처리되었습니다.",
                                                      "data": {
                                                        "message": "구글 로그인이 완료되었습니다.",
                                                        "member": {
                                                          "memberId": 456,
                                                          "email": "user@gmail.com",
                                                          "name": "이영희",
                                                          "picture": "https://lh3.googleusercontent.com/...",
                                                          "role": "USER",
                                                          "isActive": true,
                                                          "createdAt": "2024-01-10T09:15:00",
                                                          "lastLoginAt": "2024-01-15T16:45:12"
                                                        },
                                                        "note": "로그인 상태가 쿠키에 저장되었습니다."
                                                      },
                                                      "timestamp": "2024-01-15T16:45:12"
                                                    }
                                                    """
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "잘못된 ID 토큰",
                            content = @Content(
                                    schema = @Schema(implementation = CommonResponse.class),
                                    examples = @ExampleObject(
                                            name = "토큰 검증 실패",
                                            value = """
                                                    {
                                                      "success": false,
                                                      "errorCode": "INVALID_TOKEN",
                                                      "message": "유효하지 않은 Google ID Token입니다.",
                                                      "timestamp": "2024-01-15T14:30:00"
                                                    }
                                                    """
                                    )
                            )
                    )
            }
    )
    ResponseEntity<CommonResponse<LoginResponse>> loginWithGoogleToken(
            @Valid @RequestBody GoogleTokenRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse
    );

    @Operation(
            summary = "구글 로그인 안내",
            description = "구글 OAuth2 로그인 URL과 안내 정보를 제공합니다. 개발자가 구글 로그인을 테스트할 때 참고용으로 사용합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "로그인 URL 정보 제공",
                            content = @Content(
                                    schema = @Schema(implementation = CommonResponse.class),
                                    examples = @ExampleObject(
                                            name = "구글 로그인 안내 응답",
                                            value = """
                                                    {
                                                      "success": true,
                                                      "message": "요청이 성공적으로 처리되었습니다.",
                                                      "data": {
                                                        "message": "구글 로그인을 시작하려면 아래 URL로 브라우저에서 직접 접속하세요.",
                                                        "loginUrl": "/oauth2/authorization/google",
                                                        "fullUrl": "http://localhost:8080/oauth2/authorization/google",
                                                        "note": "Swagger에서 API 테스트가 필요하면 /auth/test-login을 사용하세요."
                                                      },
                                                      "timestamp": "2024-01-15T14:30:00"
                                                    }
                                                    """
                                    )
                            )
                    )
            }
    )
    ResponseEntity<CommonResponse<GoogleLoginGuideResponse>> loginWithGoogle();

    @Operation(
            summary = "토큰 갱신",
            description = "Refresh Token으로 새로운 Access Token을 발급받습니다. Access Token이 만료되었을 때 자동으로 호출됩니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "토큰 갱신 성공",
                            content = @Content(
                                    schema = @Schema(implementation = CommonResponse.class),
                                    examples = @ExampleObject(
                                            name = "토큰 갱신 성공",
                                            value = """
                                                    {
                                                      "success": true,
                                                      "message": "요청이 성공적으로 처리되었습니다.",
                                                      "data": {
                                                        "message": "Token refreshed successfully"
                                                      },
                                                      "timestamp": "2024-01-15T14:30:00"
                                                    }
                                                    """
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "유효하지 않은 Refresh Token",
                            content = @Content(
                                    schema = @Schema(implementation = CommonResponse.class),
                                    examples = {
                                            @ExampleObject(
                                                    name = "토큰이 없는 경우",
                                                    value = """
                                                            {
                                                              "success": false,
                                                              "errorCode": "NO_REFRESH_TOKEN",
                                                              "message": "Refresh token이 없습니다.",
                                                              "timestamp": "2024-01-15T14:30:00"
                                                            }
                                                            """
                                            ),
                                            @ExampleObject(
                                                    name = "토큰이 만료된 경우",
                                                    value = """
                                                            {
                                                              "success": false,
                                                              "errorCode": "REFRESH_TOKEN_EXPIRED",
                                                              "message": "Refresh token이 만료되었습니다.",
                                                              "timestamp": "2024-01-15T14:30:00"
                                                            }
                                                            """
                                            )
                                    }
                            )
                    )
            }
    )
    ResponseEntity<CommonResponse<TokenRefreshResponse>> refreshToken(
            HttpServletRequest request,
            HttpServletResponse response
    );

    @Operation(
            summary = "로그아웃",
            description = "로그아웃하고 모든 토큰을 무효화합니다. 서버에서 토큰을 블랙리스트에 등록하고 쿠키를 삭제합니다.",
            security = @SecurityRequirement(name = "cookieAuth"),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "로그아웃 성공",
                            content = @Content(
                                    schema = @Schema(implementation = CommonResponse.class),
                                    examples = @ExampleObject(
                                            name = "로그아웃 성공",
                                            value = """
                                                    {
                                                      "success": true,
                                                      "message": "로그아웃되었습니다.",
                                                      "data": null,
                                                      "timestamp": "2024-01-15T14:30:00"
                                                    }
                                                    """
                                    )
                            )
                    )
            }
    )
    ResponseEntity<CommonResponse<Void>> logout(
            @AuthenticationPrincipal UserDetails userDetails,
            HttpServletRequest request,
            HttpServletResponse response
    );

    @Operation(
            summary = "현재 사용자 정보",
            description = "JWT 토큰으로 현재 로그인한 사용자 정보를 가져옵니다. 인증이 필요한 API 호출 전에 사용자 상태를 확인할 때 사용합니다.",
            security = @SecurityRequirement(name = "cookieAuth"),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "사용자 정보 조회 성공",
                            content = @Content(
                                    schema = @Schema(implementation = CommonResponse.class),
                                    examples = @ExampleObject(
                                            name = "사용자 정보",
                                            value = """
                                                    {
                                                      "success": true,
                                                      "message": "요청이 성공적으로 처리되었습니다.",
                                                      "data": {
                                                        "memberId": 789,
                                                        "email": "user@gmail.com",
                                                        "name": "박민수",
                                                        "picture": "https://lh3.googleusercontent.com/...",
                                                        "role": "USER",
                                                        "isActive": true,
                                                        "createdAt": "2024-01-05T11:20:00",
                                                        "lastLoginAt": "2024-01-15T18:10:45"
                                                      },
                                                      "timestamp": "2024-01-15T18:10:45"
                                                    }
                                                    """
                                    )
                            )
                    )
            }
    )
    ResponseEntity<CommonResponse<?>> getCurrentUser(
            @AuthenticationPrincipal UserDetails userDetails
    );

    @Operation(
            summary = "인증 상태 확인",
            description = "현재 인증 상태를 확인합니다. 로그인 여부와 사용자 정보를 동시에 확인할 수 있습니다. 인증이 안된 경우에도 에러가 아닌 정상 응답을 반환합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "인증 상태 확인 완료",
                            content = @Content(
                                    schema = @Schema(implementation = CommonResponse.class),
                                    examples = {
                                            @ExampleObject(
                                                    name = "인증된 사용자",
                                                    value = """
                                                            {
                                                              "success": true,
                                                              "message": "요청이 성공적으로 처리되었습니다.",
                                                              "data": {
                                                                "authenticated": true,
                                                                "member": {
                                                                  "memberId": 101,
                                                                  "email": "user@gmail.com",
                                                                  "name": "최지영",
                                                                  "picture": "https://lh3.googleusercontent.com/...",
                                                                  "role": "USER",
                                                                  "isActive": true,
                                                                  "createdAt": "2024-01-01T12:00:00",
                                                                  "lastLoginAt": "2024-01-15T20:30:15"
                                                                }
                                                              },
                                                              "timestamp": "2024-01-15T20:30:15"
                                                            }
                                                            """
                                            ),
                                            @ExampleObject(
                                                    name = "인증되지 않은 사용자",
                                                    value = """
                                                            {
                                                              "success": true,
                                                              "message": "요청이 성공적으로 처리되었습니다.",
                                                              "data": {
                                                                "authenticated": false,
                                                                "member": null
                                                              },
                                                              "timestamp": "2024-01-15T20:30:15"
                                                            }
                                                            """
                                            )
                                    }
                            )
                    )
            }
    )
    ResponseEntity<CommonResponse<AuthStatusResponse>> checkAuthStatus(
            @AuthenticationPrincipal UserDetails userDetails
    );
}