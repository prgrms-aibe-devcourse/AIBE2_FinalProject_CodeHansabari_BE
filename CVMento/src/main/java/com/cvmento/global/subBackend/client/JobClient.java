package com.cvmento.global.subBackend.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;

/**
 * 작업 관리 관련 Sub Backend 클라이언트
 * 백그라운드 작업(크롤링, 특징추출, 중복제거 등)의 시작, 조회, 상태 확인을 담당
 */
@FeignClient(
        name = "sub-backend-job",
        url = "${sub-backend.url}",
        configuration = SubBackendFeignConfig.class
)
public interface JobClient {

    /**
     * 작업 시작 (크롤링, 특징추출, 중복제거 등)
     */
    @PostMapping("/api/internal/job-start")
    Map<String, Object> startJob(@RequestBody Map<String, Object> jobRequest);

    /**
     * 모든 작업 조회 (관리자용)
     */
    @GetMapping("/api/admin/jobs")
    List<Map<String, Object>> getAllJobs();

    /**
     * 작업 목록 페이징 조회 (필터링 지원)
     */
    @GetMapping("/api/internal/jobs")
    Page<Map<String, Object>> getJobs(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size,
            @RequestParam(value = "sort", defaultValue = "createdAt,desc") String sort,
            @RequestParam(value = "filter", required = false) String filter
    );

    /**
     * 특정 작업 상태 조회
     */
    @GetMapping("/api/internal/job-status/{jobId}")
    ResponseEntity<Map<String, Object>> getJobStatus(@PathVariable String jobId);

    /**
     * 특징 추출 작업 시작 (직접 호출)
     */
    @PostMapping("/api/internal/features/extract/start")
    ResponseEntity<Map<String, Object>> startFeatureExtraction();

    /**
     * 중복 제거 작업 시작 (직접 호출)
     */
    @PostMapping("/api/internal/features/dedup/start")
    ResponseEntity<Map<String, Object>> startDeduplication();

    /**
     * 최근 활성 Job 상태 조회 (중복 요청 방지용)
     */
    @GetMapping("/api/internal/job-status/latest")
    ResponseEntity<Map<String, Object>> getLatestActiveJobStatus();
}
