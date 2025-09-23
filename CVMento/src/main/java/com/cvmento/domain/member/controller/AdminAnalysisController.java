package com.cvmento.domain.member.controller;

import com.cvmento.global.common.dto.CommonResponse;
import com.cvmento.global.exception.customException.InvalidAnalysisStepException;
import com.cvmento.global.subBackend.dto.response.StepStatusResponse;
import com.cvmento.global.subBackend.service.SubBackendStatusService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

@RestController
@RequestMapping("/api/v1/admin/analysis")
@RequiredArgsConstructor
@Slf4j
public class AdminAnalysisController {

    private final SubBackendStatusService subBackendStatusService;

    private final Set<String> VALID_STEPS = Set.of("crawling", "llm-analysis", "deduplication");

    /**
     * 분석 상태 조회 (서브 백엔드와 동일한 구조)
     * GET /api/v1/admin/analysis/status                    # 최신 작업 상태
     * GET /api/v1/admin/analysis/status?      # 크롤링 단계만
     * GET /api/v1/admin/analysis/status?step=llm-analysis  # LLM 분석 단계만
     * GET /api/v1/admin/analysis/status?step=deduplication # 중복제거 단계만
     */

    @GetMapping("/status")
    public ResponseEntity<CommonResponse<StepStatusResponse>> getAnalysisStatus(
            @RequestParam(required = false) String step) {

        // 입력 검증
        if (step != null && !step.trim().isEmpty() && !VALID_STEPS.contains(step.toLowerCase())) {
            throw new InvalidAnalysisStepException("잘못된 단계명: " + step + ". 사용 가능한 값: crawling, llm-analysis, deduplication");
        }

        try {
            StepStatusResponse status;

            if (step == null || step.trim().isEmpty()) {
                status = subBackendStatusService.getSubBackendOverallStatus();
            } else {
                status = subBackendStatusService.getSubBackendStepStatus(step);
            }

            return ResponseEntity.ok(CommonResponse.success("분석 상태 조회 성공", status));

        } catch (Exception e) {
            log.error("Failed to get analysis status", e);
            return ResponseEntity.internalServerError()
                    .body(CommonResponse.error("STATUS_ERROR", "상태 조회 실패"));
        }
    }
}
