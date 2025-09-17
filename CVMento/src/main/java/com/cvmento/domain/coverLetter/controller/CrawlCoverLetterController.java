package com.cvmento.domain.coverLetter.controller;

import com.cvmento.domain.coverLetter.controller.interfaces.CrawlCoverLetterControllerInterface;
import com.cvmento.domain.coverLetter.dto.request.UpdateCrawlCoverLetterRequest;
import com.cvmento.domain.coverLetter.dto.response.CrawlCoverLetterData;
import com.cvmento.domain.coverLetter.dto.response.CrawlCoverLetterPageResponse;
import com.cvmento.domain.coverLetter.dto.response.CrawlCoverLetterResponse;
import com.cvmento.domain.coverLetter.service.CrawlCoverLetterService;
import com.cvmento.global.common.dto.CommonResponse;
import com.cvmento.global.exception.customException.CrawlCoverLetterException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 크롤링 데이터 관리자 API
 */
@RestController
@RequestMapping("/api/crawl")
@RequiredArgsConstructor
@Slf4j
public class CrawlCoverLetterController implements CrawlCoverLetterControllerInterface {

    private final CrawlCoverLetterService crawlCoverLetterService;

    /**
     * 합격 자소서 크롤링 실행
     */
    @PostMapping("/cover-letters")
    @Override
    public ResponseEntity<CommonResponse<?>> crawlCoverLetters(@AuthenticationPrincipal UserDetails userDetails) {
        MDC.put("spanId", "crawl-execution-controller");

        String userEmail = userDetails.getUsername();
        log.info("자소서 크롤링 실행 요청 - 사용자: {}", userEmail);

        try {
            CrawlCoverLetterResponse response =
                    crawlCoverLetterService.crawlAndSaveCoverLetters();

            if (response.success()) {
                log.info("크롤링 실행 성공 - 수집개수: {}", response.crawledCount());
                return ResponseEntity.ok(CommonResponse.success(response));
            } else {
                log.error("크롤링 실행 실패 - 메시지: {}", response.message());
                return ResponseEntity.ok(CommonResponse.error("CRAWLING_FAILED", response.message()));
            }

        } catch (Exception e) {
            log.error("크롤링 컨트롤러 예외 발생", e);
            return ResponseEntity.ok(CommonResponse.error("CRAWLING_ERROR", "크롤링 중 오류가 발생했습니다: " + e.getMessage()));
        }
    }

    /**
     * 크롤링 데이터 전체 조회 (페이징 없음 - 기존 호환성 유지)
     */
    @GetMapping("/cover-letters")
    @Override
    public ResponseEntity<CommonResponse<List<CrawlCoverLetterData>>> getAllCrawlCoverLetters(@AuthenticationPrincipal UserDetails userDetails) {
        MDC.put("spanId", "crawl-list-controller");

        String userEmail = userDetails.getUsername();
        log.info("크롤링 데이터 전체 조회 요청 - 사용자: {}", userEmail);

        List<CrawlCoverLetterData> coverLetters = crawlCoverLetterService.getAllCrawlCoverLetters();

        log.info("크롤링 데이터 조회 완료 - 총 개수: {}", coverLetters.size());
        return ResponseEntity.ok(CommonResponse.success(coverLetters));
    }

    /**
     * 크롤링 데이터 페이징 조회
     */
    @GetMapping("/cover-letters/paged")
    public ResponseEntity<CommonResponse<CrawlCoverLetterPageResponse>> getCrawlCoverLettersWithPagination(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal UserDetails userDetails) {
        MDC.put("spanId", "crawl-pagination-controller");

        String userEmail = userDetails.getUsername();
        
        // 페이지 크기 제한 (최대 100개)
        if (size > 100) {
            size = 100;
        }

        log.info("크롤링 데이터 페이징 조회 요청 - 사용자: {}, 페이지: {}, 크기: {}", 
                userEmail, page, size);

        CrawlCoverLetterPageResponse response = crawlCoverLetterService.getCrawlCoverLettersWithPagination(page, size);

        log.info("크롤링 데이터 페이징 조회 완료 - 총 개수: {}, 총 페이지: {}", 
                response.totalElements(), response.totalPages());
        
        return ResponseEntity.ok(CommonResponse.success(response));
    }

    /**
     * 크롤링 데이터 단건 조회
     */
    @GetMapping("/cover-letters/{id}")
    @Override
    public ResponseEntity<CommonResponse<CrawlCoverLetterData>> getCrawlCoverLetterById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        MDC.put("spanId", "crawl-detail-controller");

        String userEmail = userDetails.getUsername();
        log.info("크롤링 데이터 개별 조회 요청 - ID: {}, 사용자: {}", id, userEmail);

        try {
            CrawlCoverLetterData coverLetter = crawlCoverLetterService.getCrawlCoverLetterById(id);
            return ResponseEntity.ok(CommonResponse.success(coverLetter));
        } catch (CrawlCoverLetterException e) {
            log.warn("크롤링 데이터 조회 실패 - ID: {}, 오류: {}", id, e.getMessage());
            throw e; // GlobalExceptionHandler가 처리하도록 위임
        }
    }

    /**
     * 크롤링 데이터 수정
     */
    @PutMapping("/cover-letters/{id}")
    @Override
    public ResponseEntity<CommonResponse<CrawlCoverLetterData>> updateCrawlCoverLetter(
            @PathVariable Long id,
            @RequestBody UpdateCrawlCoverLetterRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        MDC.put("spanId", "crawl-update-controller");

        String userEmail = userDetails.getUsername();
        log.info("크롤링 데이터 수정 요청 - ID: {}, 사용자: {}, 텍스트길이: {}",
                id, userEmail, request.text() != null ? request.text().length() : 0);

        try {
            CrawlCoverLetterData updatedCoverLetter = crawlCoverLetterService.updateCrawlCoverLetter(id, request, userEmail);
            return ResponseEntity.ok(CommonResponse.success(updatedCoverLetter));
        } catch (CrawlCoverLetterException e) {
            log.error("크롤링 데이터 수정 실패 - ID: {}, 오류: {}", id, e.getMessage());
            throw e; // GlobalExceptionHandler가 처리하도록 위임
        }
    }

    /**
     * 크롤링 데이터 단건 삭제
     */
    @DeleteMapping("/cover-letters/{id}")
    @Override
    public ResponseEntity<CommonResponse<Void>> deleteCrawlCoverLetter(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        MDC.put("spanId", "crawl-delete-controller");

        String userEmail = userDetails.getUsername();
        log.info("크롤링 데이터 개별 삭제 요청 - ID: {}, 사용자: {}", id, userEmail);

        try {
            crawlCoverLetterService.deleteCrawlCoverLetter(id);
            return ResponseEntity.ok(CommonResponse.success("크롤링 데이터가 삭제되었습니다."));
        } catch (CrawlCoverLetterException e) {
            log.error("크롤링 데이터 삭제 실패 - ID: {}, 오류: {}", id, e.getMessage());
            throw e; // GlobalExceptionHandler가 처리하도록 위임
        }
    }

    /**
     * 크롤링 데이터 전체 삭제
     */
    @DeleteMapping("/cover-letters")
    @Override
    public ResponseEntity<CommonResponse<Void>> deleteAllCrawlCoverLetters(@AuthenticationPrincipal UserDetails userDetails) {
        MDC.put("spanId", "crawl-delete-all-controller");

        String userEmail = userDetails.getUsername();
        log.info("크롤링 데이터 전체 삭제 요청 - 사용자: {}", userEmail);

        crawlCoverLetterService.deleteAllCrawlCoverLetters();
        return ResponseEntity.ok(CommonResponse.success("모든 크롤링 데이터가 삭제되었습니다."));
    }
}