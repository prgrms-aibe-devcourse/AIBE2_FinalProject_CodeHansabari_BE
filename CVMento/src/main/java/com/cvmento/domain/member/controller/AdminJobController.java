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
@RequestMapping("/api/admin/jobs")
@RequiredArgsConstructor
@Slf4j
public class AdminJobController {

    private final JobClient jobClient;

    /**
     * 백그라운드 작업 히스토리 조회
     * GET /api/admin/jobs
     */
    @GetMapping
    public ResponseEntity<CommonResponse<List<Map<String, Object>>>> getJobHistory() {
        try {
            List<Map<String, Object>> jobs = jobClient.getAllJobs();
            return ResponseEntity.ok(CommonResponse.success("작업 히스토리 조회 성공", jobs));
        } catch (Exception e) {
            log.error("Failed to get job history", e);
            return ResponseEntity.internalServerError()
                    .body(CommonResponse.error("JOB_HISTORY_ERROR", "작업 히스토리 조회 실패"));
        }
    }
}