package com.cvmento.global.usage.controller;

import com.cvmento.global.common.dto.CommonResponse;
import com.cvmento.global.usage.dto.TokenUsageInfo;
import com.cvmento.global.usage.service.UsageTokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "토큰 사용량 관리", description = "토큰 사용량 조회 및 관리 API")
@RestController
@RequestMapping("/api/v1/usage")
@RequiredArgsConstructor
@Slf4j
public class UsageController {

    private final UsageTokenService usageTokenService;

    @Operation(
            summary = "현재 토큰 사용량 조회",
            description = """
                사용자의 현재 토큰 보유량과 관련 정보를 조회합니다.
                
                토큰은 다양한 AI 기능 사용 시 소모됩니다. 각 기능별 소모량은 상이하며,
                주기적으로 자동 충전됩니다.
                """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "토큰 사용량 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CommonResponse.class),
                            examples = @ExampleObject(
                                    name = "성공 응답 예시",
                                    value = """
                    {
                        "success": true,
                        "message": "토큰 사용량 조회 성공",
                        "data": {
                            "remainingTokens": 75,
                            "maxTokens": 100,
                            "nextRefillTime": "2025-09-10T09:00:00",
                            "refillAmount": 25
                        },
                        "errorCode": null,
                        "canRetry": null,
                        "timestamp": "2025-09-09T10:30:00"
                    }
                    """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증되지 않은 사용자",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                    {
                        "timestamp": "2025-09-09T10:30:00",
                        "status": 401,
                        "error": "Unauthorized",
                        "message": "인증이 필요합니다"
                    }
                    """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "사용자를 찾을 수 없음",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                    {
                        "timestamp": "2025-09-09T10:30:00",
                        "status": 404,
                        "error": "Not Found",
                        "errorCode": "MEMBER_NOT_FOUND",
                        "message": "사용자를 찾을 수 없습니다",
                        "errors": {}
                    }
                    """
                            )
                    )
            )
    })
    @GetMapping("/tokens")
    public ResponseEntity<CommonResponse<TokenUsageInfo>> getTokenUsage(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        MDC.put("spanId", "token-usage-controller");

        String email = userDetails.getUsername();

        log.info("토큰 사용량 조회 요청");

        TokenUsageInfo tokenUsage = usageTokenService.getTokenUsage(email);

        log.info("토큰 사용량 조회 완료 - 남은토큰: {}/{}",
                tokenUsage.remainingTokens(), tokenUsage.maxTokens());

        return ResponseEntity.ok(CommonResponse.success("토큰 사용량 조회 성공", tokenUsage));
    }
}