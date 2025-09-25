package com.cvmento.domain.coverLetter.controller;

import com.cvmento.domain.coverLetter.controller.interfaces.CoverLetterFeatureControllerInterface;
import com.cvmento.domain.coverLetter.service.AsyncJobService;
import com.cvmento.global.common.dto.CommonResponse;
import com.cvmento.global.subBackend.client.FeatureClient;
import com.cvmento.global.subBackend.client.JobClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * 자소서 특징 추출 및 조회 API (Sub Backend 위임)
 */
@RestController
@RequestMapping("/api/cover-letter-features")
@RequiredArgsConstructor
@Slf4j
public class CoverLetterFeatureController implements CoverLetterFeatureControllerInterface {

    private final JobClient jobClient;
    private final FeatureClient featureClient;
    private final AsyncJobService asyncJobService;

    /**
     * 크롤링된 자소서에서 특징 추출 (Sub 백엔드로 위임) - 기존 동기 방식
     */
    @PostMapping("/extract")
    @Override
    public ResponseEntity<CommonResponse<Map<String, Object>>> extractFeatures(@AuthenticationPrincipal UserDetails userDetails) {
        MDC.put("spanId", "feature-extraction-controller");

        String userEmail = userDetails.getUsername();
        log.info("자소서 특징 추출 실행 요청 - 사용자: {}", userEmail);

        try {
            // 1. 먼저 활성 Job이 있는지 확인
            ResponseEntity<Map<String, Object>> activeJobResponse = jobClient.getLatestActiveJobStatus();
            Map<String, Object> activeJobStatus = activeJobResponse.getBody();

            if (activeJobStatus != null && (Boolean) activeJobStatus.getOrDefault("hasActiveJob", false)) {
                String activeJobType = (String) activeJobStatus.get("jobType");
                String activeStatus = (String) activeJobStatus.get("status");
                String activeJobCreatedBy = (String) activeJobStatus.getOrDefault("createdBy", "SYSTEM");

                log.warn("특징 추출 요청 거부 - 활성 Job 존재: {} ({}), 생성자: {}, 요청자: {}",
                        activeJobType, activeStatus, activeJobCreatedBy, userEmail);

                return ResponseEntity.badRequest().body(CommonResponse.error(
                        "JOB_ALREADY_ACTIVE",
                        String.format("현재 %s 작업이 진행 중입니다. 작업 완료 후 다시 시도해주세요. (진행중인 작업 생성자: %s)",
                                getJobTypeKorean(activeJobType), activeJobCreatedBy)
                ));
            }

            // 2. 활성 Job이 없으면 특징 추출 시작
            Map<String, Object> jobRequest = Map.of(
                    "jobId", java.util.UUID.randomUUID().toString(),
                    "task", "FEATURE_EXTRACTION"
            );
            Map<String, Object> response = jobClient.startJob(jobRequest);
            log.info("Sub 백엔드 특징 추출 요청 성공 - 사용자: {}", userEmail);

            return ResponseEntity.ok(CommonResponse.success("특징 추출 작업이 시작되었습니다.", response));
        } catch (Exception e) {
            log.error("Sub 백엔드 특징 추출 요청 실패 - 사용자: {}, 에러: {}", userEmail, e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(CommonResponse.error("FEATURE_EXTRACTION_ERROR", "특징 추출 요청 실패"));
        }
    }

    /**
     * 임베딩 기반 특징 중복제거 수행 (Sub 백엔드로 위임) - 비동기 처리
     */
    @PostMapping("/deduplicate")
    @Override
    public ResponseEntity<CommonResponse<Map<String, Object>>> deduplicateFeatures(@AuthenticationPrincipal UserDetails userDetails) {
        MDC.put("spanId", "feature-deduplication-controller");

        String userEmail = userDetails.getUsername();
        log.info("임베딩 기반 특징 중복제거 실행 요청 - 사용자: {}", userEmail);

        try {
            // 1. 먼저 활성 Job이 있는지 확인
            ResponseEntity<Map<String, Object>> activeJobResponse = jobClient.getLatestActiveJobStatus();
            Map<String, Object> activeJobStatus = activeJobResponse.getBody();

            if (activeJobStatus != null && (Boolean) activeJobStatus.getOrDefault("hasActiveJob", false)) {
                String activeJobType = (String) activeJobStatus.get("jobType");
                String activeStatus = (String) activeJobStatus.get("status");
                String activeJobCreatedBy = (String) activeJobStatus.getOrDefault("createdBy", "SYSTEM");

                log.warn("중복제거 요청 거부 - 활성 Job 존재: {} ({}), 생성자: {}, 요청자: {}",
                        activeJobType, activeStatus, activeJobCreatedBy, userEmail);

                return ResponseEntity.badRequest().body(CommonResponse.error(
                        "JOB_ALREADY_ACTIVE",
                        String.format("현재 %s 작업이 진행 중입니다. 작업 완료 후 다시 시도해주세요. (진행중인 작업 생성자: %s)",
                                getJobTypeKorean(activeJobType), activeJobCreatedBy)
                ));
            }

            // 2. 비동기로 중복제거 작업 시작 - 별도 서비스 사용
            String jobId = java.util.UUID.randomUUID().toString();
            Map<String, Object> jobRequest = Map.of(
                    "jobId", jobId,
                    "task", "DEDUPLICATION"
            );

            // 비동기 서비스 호출 (프록시를 통해 정상 동작)
            asyncJobService.executeDeduplicationAsync(jobRequest, userEmail);

            // 즉시 응답 반환
            Map<String, Object> responseData = Map.of(
                    "jobId", jobId,
                    "status", "STARTED",
                    "message", "중복제거 작업이 백그라운드에서 시작되었습니다."
            );

            log.info("중복제거 작업 시작 응답 - 사용자: {}, jobId: {}", userEmail, jobId);
            return ResponseEntity.ok(CommonResponse.success("특징 중복제거 작업이 시작되었습니다.", responseData));

        } catch (Exception e) {
            log.error("Sub 백엔드 특징 중복제거 요청 실패 - 사용자: {}, 에러: {}", userEmail, e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(CommonResponse.error("FEATURE_DEDUPLICATION_ERROR", "특징 중복제거 요청 실패"));
        }
    }

    /**
     * 자소서 특징 추출 및 중복 제거 전체 프로세스 실행 (Sub 백엔드로 위임)
     */
    @PostMapping("/process-all")
    @Override
    public ResponseEntity<CommonResponse<Object>> extractFeaturesWithRealtimeAPI(@AuthenticationPrincipal UserDetails userDetails) {
        MDC.put("spanId", "feature-process-all-controller");

        String userEmail = userDetails.getUsername();
        log.info("전체 특징 처리 프로세스 실행 요청 - 사용자: {}", userEmail);

        try {
            // 1. 먼저 활성 Job이 있는지 확인
            ResponseEntity<Map<String, Object>> activeJobResponse = jobClient.getLatestActiveJobStatus();
            Map<String, Object> activeJobStatus = activeJobResponse.getBody();

            if (activeJobStatus != null && (Boolean) activeJobStatus.getOrDefault("hasActiveJob", false)) {
                String activeJobType = (String) activeJobStatus.get("jobType");
                String activeStatus = (String) activeJobStatus.get("status");
                String activeJobCreatedBy = (String) activeJobStatus.getOrDefault("createdBy", "SYSTEM");

                log.warn("전체 특징 처리 요청 거부 - 활성 Job 존재: {} ({}), 생성자: {}, 요청자: {}",
                        activeJobType, activeStatus, activeJobCreatedBy, userEmail);

                return ResponseEntity.badRequest().body(CommonResponse.error(
                        "JOB_ALREADY_ACTIVE",
                        String.format("현재 %s 작업이 진행 중입니다. 작업 완료 후 다시 시도해주세요. (진행중인 작업 생성자: %s)",
                                getJobTypeKorean(activeJobType), activeJobCreatedBy)
                ));
            }

            // 2. 활성 Job이 없으면 전체 특징 처리 프로세스 시작
            Map<String, Object> jobRequest = Map.of(
                    "jobId", java.util.UUID.randomUUID().toString(),
                    "task", "FEATURE_PROCESS_ALL"
            );
            Map<String, Object> response = jobClient.startJob(jobRequest);
            log.info("Sub 백엔드 전체 특징 처리 요청 성공 - 사용자: {}", userEmail);

            return ResponseEntity.ok(CommonResponse.success("전체 특징 처리 작업이 시작되었습니다.", response));
        } catch (Exception e) {
            log.error("Sub 백엔드 전체 특징 처리 요청 실패 - 사용자: {}, 에러: {}", userEmail, e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(CommonResponse.error("FEATURE_PROCESS_ALL_ERROR", "전체 특징 처리 요청 실패"));
        }
    }

    /**
     * 모든 특징을 페이징으로 조회 (Sub 백엔드로 위임)
     */
    @GetMapping("/")
    // @Override // 임시로 제거 - 인터페이스 미완성
    public ResponseEntity<CommonResponse<Page<Map<String, Object>>>> getAllFeaturesWithPagination(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            @AuthenticationPrincipal UserDetails userDetails) {

        MDC.put("spanId", "feature-query-controller-all");

        String userEmail = userDetails.getUsername();
        log.info("모든 특징 페이징 조회 요청 - 사용자: {}, 페이지: {}, 크기: {}",
                userEmail, pageable.getPageNumber(), pageable.getPageSize());

        try {
            Page<Map<String, Object>> response = featureClient.getAllFeatures(
                    pageable.getPageNumber(),
                    pageable.getPageSize(),
                    "createdAt,desc"
            );

            log.info("모든 특징 페이징 조회 완료 - 총 개수: {}, 총 페이지: {}",
                    response.getTotalElements(), response.getTotalPages());

            return ResponseEntity.ok(CommonResponse.success(response));
        } catch (Exception e) {
            log.error("Sub 백엔드 특징 조회 실패 - 사용자: {}, 에러: {}", userEmail, e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(CommonResponse.error("FEATURE_FETCH_ERROR", "특징 조회 실패"));
        }
    }

    /**
     * 특정 카테고리의 특징들을 페이징으로 조회 (Sub 백엔드로 위임)
     */
    @GetMapping("/category/{category}")
    // @Override // 임시로 제거 - 인터페이스 미완성
    public ResponseEntity<CommonResponse<Page<Map<String, Object>>>> getFeaturesByCategoryWithPagination(
            @PathVariable String category,
            @PageableDefault(size = 20, sort = "duplicateCount", direction = Sort.Direction.DESC) Pageable pageable,
            @AuthenticationPrincipal UserDetails userDetails) {

        MDC.put("spanId", "feature-query-controller-category");

        String userEmail = userDetails.getUsername();
        log.info("카테고리별 특징 페이징 조회 요청 - 사용자: {}, 카테고리: {}, 페이지: {}, 크기: {}",
                userEmail, category, pageable.getPageNumber(), pageable.getPageSize());

        try {
            Page<Map<String, Object>> response = featureClient.getFeaturesByCategory(
                    category,
                    pageable.getPageNumber(),
                    pageable.getPageSize(),
                    "duplicateCount,desc"
            );

            log.info("카테고리별 특징 페이징 조회 완료 - 카테고리: {}, 총 개수: {}, 총 페이지: {}",
                    category, response.getTotalElements(), response.getTotalPages());

            return ResponseEntity.ok(CommonResponse.success(response));
        } catch (Exception e) {
            log.error("Sub 백엔드 카테고리별 특징 조회 실패 - 사용자: {}, 카테고리: {}, 에러: {}", userEmail, category, e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(CommonResponse.error("FEATURE_CATEGORY_FETCH_ERROR", "카테고리별 특징 조회 실패"));
        }
    }

    /**
     * 특징 통계 정보 조회 (Sub 백엔드로 위임)
     */
    @GetMapping("/statistics")
    // @Override // 임시로 제거 - 인터페이스 미완성
    public ResponseEntity<CommonResponse<Map<String, Object>>> getFeatureStatistics(
            @AuthenticationPrincipal UserDetails userDetails) {

        MDC.put("spanId", "feature-query-controller-statistics");

        String userEmail = userDetails.getUsername();
        log.info("특징 통계 조회 요청 - 사용자: {}", userEmail);

        try {
            ResponseEntity<Map<String, Object>> response = featureClient.getFeatureStatistics();

            log.info("특징 통계 조회 완료 - 사용자: {}", userEmail);

            return ResponseEntity.ok(CommonResponse.success(response.getBody()));
        } catch (Exception e) {
            log.error("Sub 백엔드 특징 통계 조회 실패 - 사용자: {}, 에러: {}", userEmail, e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(CommonResponse.error("FEATURE_STATISTICS_ERROR", "특징 통계 조회 실패"));
        }
    }

    /**
     * Raw 특징 페이징 조회 (Sub 백엔드로 위임)
     */
    @GetMapping("/raw")
    @Override
    public ResponseEntity<CommonResponse<Page<Map<String, Object>>>> getRawFeaturesPaged(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            @AuthenticationPrincipal UserDetails userDetails) {
        MDC.put("spanId", "raw-feature-query-controller-all");

        String userEmail = userDetails.getUsername();
        log.info("Raw 특징 페이징 조회 요청 - 사용자: {}, 페이지: {}, 크기: {}", userEmail, pageable.getPageNumber(), pageable.getPageSize());

        try {
            Page<Map<String, Object>> response = featureClient.getRawFeatures(
                    pageable.getPageNumber(),
                    pageable.getPageSize(),
                    "createdAt,desc"
            );

            log.info("Raw 특징 페이징 조회 완료 - 총 개수: {}, 총 페이지: {}",
                    response.getTotalElements(), response.getTotalPages());

            return ResponseEntity.ok(CommonResponse.success(response));
        } catch (Exception e) {
            log.error("Sub 백엔드 Raw 특징 조회 실패 - 사용자: {}, 에러: {}", userEmail, e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(CommonResponse.error("RAW_FEATURE_FETCH_ERROR", "Raw 특징 조회 실패"));
        }
    }

    /**
     * 카테고리별 Raw 특징 페이징 조회 (Sub 백엔드로 위임)
     */
    @GetMapping("/raw/category/{category}")
    @Override
    public ResponseEntity<CommonResponse<Page<Map<String, Object>>>> getRawFeaturesByCategoryPaged(
            @PathVariable String category,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            @AuthenticationPrincipal UserDetails userDetails) {
        MDC.put("spanId", "raw-feature-query-controller-category");

        String userEmail = userDetails.getUsername();
        log.info("Raw 카테고리별 특징 페이징 조회 요청 - 사용자: {}, 카테고리: {}, 페이지: {}, 크기: {}", userEmail, category, pageable.getPageNumber(), pageable.getPageSize());

        try {
            Page<Map<String, Object>> response = featureClient.getRawFeaturesByCategory(
                    category,
                    pageable.getPageNumber(),
                    pageable.getPageSize(),
                    "createdAt,desc"
            );

            log.info("Raw 카테고리별 특징 페이징 조회 완료 - 카테고리: {}, 총 개수: {}, 총 페이지: {}",
                    category, response.getTotalElements(), response.getTotalPages());

            return ResponseEntity.ok(CommonResponse.success(response));
        } catch (Exception e) {
            log.error("Sub 백엔드 Raw 카테고리별 특징 조회 실패 - 사용자: {}, 카테고리: {}, 에러: {}", userEmail, category, e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(CommonResponse.error("RAW_FEATURE_CATEGORY_FETCH_ERROR", "Raw 카테고리별 특징 조회 실패"));
        }
    }

    /**
     * Job 타입을 한국어로 변환하는 유틸리티 메서드
     */
    private String getJobTypeKorean(String jobType) {
        return switch (jobType) {
            case "CRAWLING" -> "크롤링";
            case "FEATURE_EXTRACTION" -> "특징 추출";
            case "DEDUPLICATION" -> "중복 제거";
            case "FEATURE_PROCESS_ALL" -> "전체 특징 처리";
            default -> jobType;
        };
    }
}