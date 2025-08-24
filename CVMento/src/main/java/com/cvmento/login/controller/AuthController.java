package com.cvmento.login.controller;

import com.cvmento.login.dto.SignupRequestDto;
import com.cvmento.login.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.security.Principal;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
@Tag(name = "Auth", description = "인증 관련 API") // ✅ Swagger 그룹 이름
public class AuthController {

    private final AuthService authService;

    /**
     * 회원가입 API
     * 이메일, 비밀번호, 닉네임, 전화번호를 통해 회원가입을 진행합니다.
     *
     * @param signupRequestDto 회원가입 요청 DTO
     * @return 회원가입 성공 또는 실패 메시지
     */
    @Operation(summary = "회원가입", description = "이메일, 비밀번호, 닉네임, 전화번호를 통해 회원가입을 진행합니다.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "회원가입 완료",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = String.class),
                            examples = @ExampleObject(value = "\"회원가입이 완료되었습니다.\"")
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "이미 존재하는 이메일",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = String.class),
                            examples = @ExampleObject(value = "\"이미 존재하는 이메일입니다.\"")
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Validation 오류",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = String.class),
                            examples = @ExampleObject(value = "\"이메일 형식이 유효하지 않습니다.\"")
                    )
            )
    })
    @PostMapping("/signup")
    public ResponseEntity<?> signup(
            @Valid @RequestBody SignupRequestDto signupRequestDto
    ) {
        log.info("회원가입 요청");
        boolean result = authService.signup(signupRequestDto);

        if (result) {
            return ResponseEntity.ok("회원가입이 완료되었습니다.");
        } else {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("이미 존재하는 이메일입니다.");
        }
    }

    /**
     * 로그아웃 요청 처리
     * 현재 로그인된 사용자를 로그아웃 처리합니다.
     *
     * @param response HTTP 응답 객체
     * @param principal 인증된 사용자 정보 (스프링 시큐리티)
     * @return 로그아웃 결과 메시지
     * @throws IOException 응답 쓰기 실패 시
     */
    @Operation(summary = "로그아웃", description = "현재 로그인된 사용자를 로그아웃 처리합니다.")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "로그아웃 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Object.class),
                            examples = @ExampleObject(value = "{\"message\": \"로그아웃 성공\"}")
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증 실패 - JWT 토큰이 없거나 유효하지 않은 경우",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = String.class),
                            examples = @ExampleObject(value = "{\"error\": \"Unauthorized: Invalid or expired JWT token\"}")
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "사용자를 찾을 수 없음",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = String.class),
                            examples = @ExampleObject(value = "\"사용자를 찾을 수 없습니다.\"")
                    )
            )
    })
    @PostMapping("/logout")
    public ResponseEntity<?> logout(
            HttpServletResponse response,
            Principal principal
    ) throws IOException {
        String email = principal.getName();
        log.info("로그아웃 요청 - email: {}", email);
        authService.logout(email, response);
        return ResponseEntity.ok().build();
    }


}
