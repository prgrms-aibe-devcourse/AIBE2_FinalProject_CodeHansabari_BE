package com.cvmento.domain.member.controller;

import com.cvmento.global.common.dto.CommonResponse;
import com.cvmento.global.subBackend.client.JobClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/analysis")
@RequiredArgsConstructor
@Slf4j
public class AdminAnalysisController {

    private final JobClient jobClient;

    /**
     * 분석 상태 조회 (BackgroundJob 기반)
     * GET /api/v1/admin/analysis/status
     */

    @GetMapping("/status")
    public ResponseEntity<CommonResponse<List<Map<String, Object>>>> getAnalysisStatus() {
        try {
            List<Map<String, Object>> jobs = jobClient.getAllJobs();
            return ResponseEntity.ok(CommonResponse.success("분석 상태 조회 성공", jobs));
        } catch (Exception e) {
            log.error("Failed to get analysis status", e);
            return ResponseEntity.internalServerError()
                    .body(CommonResponse.error("STATUS_ERROR", "상태 조회 실패"));
        }
    }
}