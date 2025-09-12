package com.cvmento.global.usage.controller;

import com.cvmento.global.common.dto.CommonResponse;
import com.cvmento.global.usage.controller.interfaces.UsageControllerInterface;
import com.cvmento.global.usage.dto.TokenUsageInfo;
import com.cvmento.global.usage.service.UsageTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/usage")
@RequiredArgsConstructor
@Slf4j
public class UsageController implements UsageControllerInterface {

    private final UsageTokenService usageTokenService;

    @Override
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