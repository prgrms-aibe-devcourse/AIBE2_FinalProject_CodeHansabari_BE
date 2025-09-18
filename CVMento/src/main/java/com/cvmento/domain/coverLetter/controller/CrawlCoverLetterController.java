package com.cvmento.domain.coverLetter.controller;

import com.cvmento.domain.coverLetter.controller.interfaces.CrawlCoverLetterControllerInterface;
import com.cvmento.domain.coverLetter.dto.request.UpdateCrawlCoverLetterRequest;
import com.cvmento.domain.coverLetter.dto.response.CrawlCoverLetterData;
import com.cvmento.domain.coverLetter.dto.response.CrawlCoverLetterResponse;
import com.cvmento.domain.coverLetter.service.CrawlCoverLetterService;
import com.cvmento.domain.coverLetter.service.CrawlCoverLetterQueryService;
import com.cvmento.global.common.dto.CommonResponse;
import com.cvmento.global.exception.customException.CrawlCoverLetterException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

/**
 * 크롤링 데이터 관리자 API
 */
@RestController
@RequestMapping("/api/crawled-cover-letters")
@RequiredArgsConstructor
@Slf4j
public class CrawlCoverLetterController implements CrawlCoverLetterControllerInterface {

    private final CrawlCoverLetterService crawlCoverLetterService;
    private final CrawlCoverLetterQueryService crawlCoverLetterQueryService;

    /**
     * 합격 자소서 크롤링 실행
     */
    @PostMapping("/")
    @Override
    public ResponseEntity<CommonResponse<?>> crawlCoverLetters(@AuthenticationPrincipal UserDetails userDetails) {
        MDC.put("spanId", "crawl-execution-controller");

        String userEmail = userDetails.getUsername();
        log.info("자소서 크롤링 실행 요청 - 사용자: {}", userEmail);

        CrawlCoverLetterResponse response = crawlCoverLetterService.crawlAndSaveCoverLetters();

        if (response.success()) {
            log.info("크롤링 실행 성공 - 수집개수: {}", response.crawledCount());
            return ResponseEntity.ok(CommonResponse.success(response));
        } else {
            log.error("크롤링 실행 실패 - 메시지: {}", response.message());
            // 실패 시에도 성공 응답으로 감싸서 보내되, 내용은 에러 DTO를 담는 것이 일관성 있을 수 있습니다.
            // 여기서는 기존 로직을 유지하되, 예외를 던지는 방식으로 변경합니다.
            throw new CrawlCoverLetterException(response.message());
        }
    }

    /**
     * 크롤링 데이터 페이징 조회
     */
    @GetMapping("/")
    @Override
    public ResponseEntity<CommonResponse<Page<CrawlCoverLetterData>>> getCrawlCoverLettersWithPagination(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            @AuthenticationPrincipal UserDetails userDetails) {
        MDC.put("spanId", "crawl-pagination-controller");

        String userEmail = userDetails.getUsername();

        log.info("크롤링 데이터 페이징 조회 요청 - 사용자: {}, 페이지: {}, 크기: {}",
                userEmail, pageable.getPageNumber(), pageable.getPageSize());

        Page<CrawlCoverLetterData> response = crawlCoverLetterQueryService.getCrawlCoverLettersWithPagination(pageable);

        log.info("크롤링 데이터 페이징 조회 완료 - 총 개수: {}, 총 페이지: {}",
                response.getTotalElements(), response.getTotalPages());
        
        return ResponseEntity.ok(CommonResponse.success(response));
    }

    /**
     * 크롤링 데이터 단건 조회
     */
    @GetMapping("/{id}")
    @Override
    public ResponseEntity<CommonResponse<CrawlCoverLetterData>> getCrawlCoverLetterById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        MDC.put("spanId", "crawl-detail-controller");

        String userEmail = userDetails.getUsername();
        log.info("크롤링 데이터 개별 조회 요청 - ID: {}, 사용자: {}", id, userEmail);

        CrawlCoverLetterData coverLetter = crawlCoverLetterQueryService.getCrawlCoverLetterById(id);
        return ResponseEntity.ok(CommonResponse.success(coverLetter));
    }

    /**
     * 크롤링 데이터 수정
     */
    @PutMapping("/{id}")
    @Override
    public ResponseEntity<CommonResponse<CrawlCoverLetterData>> updateCrawlCoverLetter(
            @PathVariable Long id,
            @RequestBody UpdateCrawlCoverLetterRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        MDC.put("spanId", "crawl-update-controller");

        String userEmail = userDetails.getUsername();
        log.info("크롤링 데이터 수정 요청 - ID: {}, 사용자: {}, 텍스트길이: {}",
                id, userEmail, request.text() != null ? request.text().length() : 0);

        CrawlCoverLetterData updatedCoverLetter = crawlCoverLetterService.updateCrawlCoverLetter(id, request, userEmail);
        return ResponseEntity.ok(CommonResponse.success(updatedCoverLetter));
    }

    /**
     * 크롤링 데이터 단건 삭제
     */
    @DeleteMapping("/{id}")
    @Override
    public ResponseEntity<CommonResponse<Void>> deleteCrawlCoverLetter(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        MDC.put("spanId", "crawl-delete-controller");

        String userEmail = userDetails.getUsername();
        log.info("크롤링 데이터 개별 삭제 요청 - ID: {}, 사용자: {}", id, userEmail);

        crawlCoverLetterService.deleteCrawlCoverLetter(id);
        return ResponseEntity.ok(CommonResponse.success("크롤링 데이터가 삭제되었습니다."));
    }

    /**
     * 크롤링 데이터 전체 삭제
     */
    @DeleteMapping("/")
    @Override
    public ResponseEntity<CommonResponse<Void>> deleteAllCrawlCoverLetters(@AuthenticationPrincipal UserDetails userDetails) {
        MDC.put("spanId", "crawl-delete-all-controller");

        String userEmail = userDetails.getUsername();
        log.info("크롤링 데이터 전체 삭제 요청 - 사용자: {}", userEmail);

        crawlCoverLetterService.deleteAllCrawlCoverLetters();
        return ResponseEntity.ok(CommonResponse.success("모든 크롤링 데이터가 삭제되었습니다."));
    }
}