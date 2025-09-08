package com.cvmento.global.usage.controller;

import com.cvmento.domain.auth.service.AuthService;
import com.cvmento.global.common.dto.CommonResponse;
import com.cvmento.global.usage.dto.TokenUsageInfo;
import com.cvmento.global.usage.service.UsageTokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
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
public class UsageController {

    private final UsageTokenService usageTokenService;

    @Operation(
            summary = "현재 토큰 사용량 조회",
            description = "사용자의 현재 토큰 보유량, 최대 토큰 수, 다음 충전 시간 등을 조회합니다."
    )
    @ApiResponse(responseCode = "200", description = "토큰 사용량 조회 성공")
    @ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
    @GetMapping("/tokens")
    public ResponseEntity<CommonResponse<TokenUsageInfo>> getTokenUsage(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        String email = userDetails.getUsername();
        TokenUsageInfo tokenUsage = usageTokenService.getTokenUsage(email);

        return ResponseEntity.ok(CommonResponse.success("토큰 사용량 조회 성공", tokenUsage));
    }
}