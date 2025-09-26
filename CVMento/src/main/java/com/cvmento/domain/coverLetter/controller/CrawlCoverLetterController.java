package com.cvmento.domain.coverLetter.controller;

import com.cvmento.domain.coverLetter.controller.interfaces.CrawlCoverLetterControllerInterface;
import com.cvmento.domain.coverLetter.dto.request.UpdateCrawlCoverLetterRequest;
import com.cvmento.global.common.dto.CommonResponse;
import com.cvmento.global.subBackend.client.CrawlClient;
import com.cvmento.global.subBackend.client.JobClient;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 크롤링 데이터 관리자 API
 */
@RestController
@RequestMapping("/api/crawled-cover-letters")
@RequiredArgsConstructor
@Slf4j
public class CrawlCoverLetterController implements CrawlCoverLetterControllerInterface {

    private final JobClient jobClient;
    private final CrawlClient crawlClient;

    /**
     * 합격 자소서 크롤링 실행 (Sub 백엔드로 위임)
     */
    @PostMapping("/")
    @Override
    public ResponseEntity<CommonResponse<?>> crawlCoverLetters(@AuthenticationPrincipal UserDetails userDetails) {
        MDC.put("spanId", "crawl-execution-controller");

        String userEmail = userDetails.getUsername();
        log.info("자소서 크롤링 실행 요청 - 사용자: {}", userEmail);

        try {
            // 1. 먼저 활성 Job이 있는지 확인
            ResponseEntity<Map<String, Object>> activeJobResponse = jobClient.getLatestActiveJobStatus();
            Map<String, Object> activeJobStatus = activeJobResponse.getBody();
            
            if (activeJobStatus != null && (Boolean) activeJobStatus.getOrDefault("hasActiveJob", false)) {
                String activeJobType = (String) activeJobStatus.get("jobType");
                String activeStatus = (String) activeJobStatus.get("status");
                String activeJobCreatedBy = (String) activeJobStatus.getOrDefault("createdBy", "SYSTEM");
                
                log.warn("크롤링 요청 거부 - 활성 Job 존재: {} ({}), 생성자: {}, 요청자: {}", 
                        activeJobType, activeStatus, activeJobCreatedBy, userEmail);
                        
                return ResponseEntity.badRequest().body(CommonResponse.error(
                        "JOB_ALREADY_ACTIVE", 
                        String.format("현재 %s 작업이 진행 중입니다. 작업 완료 후 다시 시도해주세요. (진행중인 작업 생성자: %s)", 
                                getJobTypeKorean(activeJobType), activeJobCreatedBy)
                ));
            }
            
            // 2. 활성 Job이 없으면 크롤링 시작
            Map<String, Object> jobRequest = Map.of(
                    "jobId", java.util.UUID.randomUUID().toString(),
                    "task", "CRAWLING"
            );
            Map<String, Object> response = jobClient.startJob(jobRequest);
            log.info("Sub 백엔드 크롤링 요청 성공 - 사용자: {}", userEmail);
            
            return ResponseEntity.ok(CommonResponse.success("크롤링 작업이 시작되었습니다.", response));
        } catch (Exception e) {
            log.error("Sub 백엔드 크롤링 요청 실패 - 사용자: {}, 에러: {}", userEmail, e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(CommonResponse.error("CRAWL_REQUEST_ERROR", "크롤링 요청 실패"));
        }
    }

    /**
     * 크롤링 데이터 페이징 조회 (Sub 백엔드로 위임)
     */
    @GetMapping("/")
    @Override
    public ResponseEntity<CommonResponse<Page<Map<String, Object>>>> getCrawlCoverLettersWithPagination(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            @AuthenticationPrincipal UserDetails userDetails) {
        MDC.put("spanId", "crawl-pagination-controller");

        String userEmail = userDetails.getUsername();

        log.info("크롤링 데이터 페이징 조회 요청 - 사용자: {}, 페이지: {}, 크기: {}",
                userEmail, pageable.getPageNumber(), pageable.getPageSize());

        try {
            Page<Map<String, Object>> response = crawlClient.getCrawlData(
                    pageable.getPageNumber(), 
                    pageable.getPageSize(), 
                    "createdAt,desc"
            );

            log.info("크롤링 데이터 페이징 조회 완료 - 총 개수: {}, 총 페이지: {}",
                    response.getTotalElements(), response.getTotalPages());
            
            return ResponseEntity.ok(CommonResponse.success(response));
        } catch (Exception e) {
            log.error("Sub 백엔드 크롤링 데이터 조회 실패 - 사용자: {}, 에러: {}", userEmail, e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(CommonResponse.error("CRAWL_DATA_FETCH_ERROR", "크롤링 데이터 조회 실패"));
        }
    }

    /**
     * 크롤링 데이터 단건 조회 (Sub 백엔드로 위임)
     */
    @GetMapping("/{id}")
    @Override
    public ResponseEntity<CommonResponse<Map<String, Object>>> getCrawlCoverLetterById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        MDC.put("spanId", "crawl-detail-controller");

        String userEmail = userDetails.getUsername();
        log.info("크롤링 데이터 개별 조회 요청 - ID: {}, 사용자: {}", id, userEmail);

        try {
            Map<String, Object> coverLetter = crawlClient.getCrawlDataById(id);
            return ResponseEntity.ok(CommonResponse.success(coverLetter));
        } catch (Exception e) {
            log.error("Sub 백엔드 크롤링 데이터 개별 조회 실패 - ID: {}, 사용자: {}, 에러: {}", id, userEmail, e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(CommonResponse.error("CRAWL_DATA_FETCH_ERROR", "크롤링 데이터 조회 실패"));
        }
    }

    /**
     * 크롤링 데이터 수정 (Sub 백엔드로 위임)
     */
    @PutMapping("/{id}")
    @Override
    public ResponseEntity<CommonResponse<Map<String, Object>>> updateCrawlCoverLetter(
            @PathVariable Long id,
            @RequestBody @Valid UpdateCrawlCoverLetterRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        MDC.put("spanId", "crawl-update-controller");

        String userEmail = userDetails.getUsername();
        log.info("크롤링 데이터 수정 요청 - ID: {}, 사용자: {}, 텍스트길이: {}",
                id, userEmail, request.text() != null ? request.text().length() : 0);

        try {
            Map<String, Object> requestBody = Map.of("text", request.text());
            Map<String, Object> updatedCoverLetter = crawlClient.updateCrawlData(id, requestBody);
            return ResponseEntity.ok(CommonResponse.success(updatedCoverLetter));
        } catch (Exception e) {
            log.error("Sub 백엔드 크롤링 데이터 수정 실패 - ID: {}, 사용자: {}, 에러: {}", id, userEmail, e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(CommonResponse.error("CRAWL_DATA_UPDATE_ERROR", "크롤링 데이터 수정 실패"));
        }
    }

    /**
     * 크롤링 데이터 단건 삭제 (Sub 백엔드로 위임)
     */
    @DeleteMapping("/{id}")
    @Override
    public ResponseEntity<CommonResponse<Void>> deleteCrawlCoverLetter(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        MDC.put("spanId", "crawl-delete-controller");

        String userEmail = userDetails.getUsername();
        log.info("크롤링 데이터 개별 삭제 요청 - ID: {}, 사용자: {}", id, userEmail);

        try {
            crawlClient.deleteCrawlData(id);
            return ResponseEntity.ok(CommonResponse.success("크롤링 데이터가 삭제되었습니다."));
        } catch (Exception e) {
            log.error("Sub 백엔드 크롤링 데이터 삭제 실패 - ID: {}, 사용자: {}, 에러: {}", id, userEmail, e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(CommonResponse.error("CRAWL_DATA_DELETE_ERROR", "크롤링 데이터 삭제 실패"));
        }
    }

    /**
     * 크롤링 데이터 전체 삭제 (Sub 백엔드로 위임)
     */
    @DeleteMapping("/")
    @Override
    public ResponseEntity<CommonResponse<Void>> deleteAllCrawlCoverLetters(@AuthenticationPrincipal UserDetails userDetails) {
        MDC.put("spanId", "crawl-delete-all-controller");

        String userEmail = userDetails.getUsername();
        log.info("크롤링 데이터 전체 삭제 요청 - 사용자: {}", userEmail);

        try {
            crawlClient.deleteAllCrawlData();
            return ResponseEntity.ok(CommonResponse.success("모든 크롤링 데이터가 삭제되었습니다."));
        } catch (Exception e) {
            log.error("Sub 백엔드 크롤링 데이터 전체 삭제 실패 - 사용자: {}, 에러: {}", userEmail, e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(CommonResponse.error("CRAWL_DATA_DELETE_ALL_ERROR", "크롤링 데이터 전체 삭제 실패"));
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
            default -> jobType;
        };
    }
}