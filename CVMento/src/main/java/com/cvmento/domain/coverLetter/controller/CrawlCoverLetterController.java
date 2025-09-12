package com.cvmento.domain.coverLetter.controller;

import com.cvmento.domain.auth.service.AuthService;
import com.cvmento.domain.coverLetter.controller.interfaces.CrawlCoverLetterControllerInterface;
import com.cvmento.domain.coverLetter.dto.request.UpdateCrawlCoverLetterRequest;
import com.cvmento.domain.coverLetter.dto.response.CrawlCoverLetterData;
import com.cvmento.domain.coverLetter.dto.response.CrawlCoverLetterResponse;
import com.cvmento.domain.coverLetter.service.CrawlCoverLetterService;
import com.cvmento.domain.member.entity.Member;
import com.cvmento.domain.member.enums.Role;
import com.cvmento.global.common.dto.CommonResponse;
import com.cvmento.global.exception.CrawlCoverLetterException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
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
    private final AuthService authService;

    /**
     * 합격 자소서 크롤링 실행
     */
    @PostMapping("/cover-letters")
    @Override
    public ResponseEntity<CommonResponse<?>> crawlCoverLetters(@AuthenticationPrincipal UserDetails userDetails) {
        MDC.put("spanId", "crawl-execution-controller");

        if (userDetails == null) {
            throw new AccessDeniedException("인증되지 않은 사용자입니다.");
        }

        Member member = authService.getMemberFromUserDetails(userDetails);
        if (member.getRole() != Role.ADMIN && member.getRole() != Role.ROOT) {
            log.warn("크롤링 권한 없는 접근 시도 - memberId: {}, role: {}",
                    member.getMemberId(), member.getRole());
            throw new AccessDeniedException("크롤링을 실행할 권한이 없습니다. 관리자 권한이 필요합니다.");
        }

        log.info("자소서 크롤링 실행 요청 - 관리자: {}, role: {}",
                member.getMemberId(), member.getRole());

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
     * 크롤링 데이터 전체 조회
     */
    @GetMapping("/cover-letters")
    @Override
    public ResponseEntity<CommonResponse<List<CrawlCoverLetterData>>> getAllCrawlCoverLetters(@AuthenticationPrincipal UserDetails userDetails) {
        MDC.put("spanId", "crawl-list-controller");

        if (userDetails == null) {
            throw new AccessDeniedException("인증되지 않은 사용자입니다.");
        }

        Member member = authService.getMemberFromUserDetails(userDetails);
        if (member.getRole() != Role.ADMIN && member.getRole() != Role.ROOT) {
            throw new AccessDeniedException("크롤링 데이터를 조회할 권한이 없습니다. 관리자 권한이 필요합니다.");
        }

        log.info("크롤링 데이터 전체 조회 요청 - 관리자: {}", member.getMemberId());

        List<CrawlCoverLetterData> coverLetters = crawlCoverLetterService.getAllCrawlCoverLetters();

        log.info("크롤링 데이터 조회 완료 - 총 개수: {}", coverLetters.size());
        return ResponseEntity.ok(CommonResponse.success(coverLetters));
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

        if (userDetails == null) {
            throw new AccessDeniedException("인증되지 않은 사용자입니다.");
        }

        Member member = authService.getMemberFromUserDetails(userDetails);
        if (member.getRole() != Role.ADMIN && member.getRole() != Role.ROOT) {
            throw new AccessDeniedException("크롤링 데이터를 조회할 권한이 없습니다. 관리자 권한이 필요합니다.");
        }

        log.info("크롤링 데이터 개별 조회 요청 - ID: {}, 관리자: {}", id, member.getMemberId());

        try {
            CrawlCoverLetterData coverLetter = crawlCoverLetterService.getCrawlCoverLetterById(id);
            return ResponseEntity.ok(CommonResponse.success(coverLetter));
        } catch (CrawlCoverLetterException e) {
            log.warn("크롤링 데이터 조회 실패 - ID: {}, 오류: {}", id, e.getMessage());
            return ResponseEntity.status(e.getHttpStatus())
                    .body(CommonResponse.error(e.getErrorCode(), e.getMessage()));
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

        if (userDetails == null) {
            throw new AccessDeniedException("인증되지 않은 사용자입니다.");
        }

        Member member = authService.getMemberFromUserDetails(userDetails);
        if (member.getRole() != Role.ADMIN && member.getRole() != Role.ROOT) {
            throw new AccessDeniedException("크롤링 데이터를 수정할 권한이 없습니다. 관리자 권한이 필요합니다.");
        }

        log.info("크롤링 데이터 수정 요청 - ID: {}, 관리자: {}, 텍스트길이: {}",
                id, member.getMemberId(), request.text() != null ? request.text().length() : 0);

        try {
            CrawlCoverLetterData updatedCoverLetter = crawlCoverLetterService.updateCrawlCoverLetter(id, request, member);
            return ResponseEntity.ok(CommonResponse.success(updatedCoverLetter));
        } catch (CrawlCoverLetterException e) {
            log.error("크롤링 데이터 수정 실패 - ID: {}, 오류: {}", id, e.getMessage());
            return ResponseEntity.status(e.getHttpStatus())
                    .body(CommonResponse.error(e.getErrorCode(), e.getMessage()));
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

        if (userDetails == null) {
            throw new AccessDeniedException("인증되지 않은 사용자입니다.");
        }

        Member member = authService.getMemberFromUserDetails(userDetails);
        if (member.getRole() != Role.ADMIN && member.getRole() != Role.ROOT) {
            throw new AccessDeniedException("크롤링 데이터를 삭제할 권한이 없습니다. 관리자 권한이 필요합니다.");
        }

        log.info("크롤링 데이터 개별 삭제 요청 - ID: {}, 관리자: {}", id, member.getMemberId());

        try {
            crawlCoverLetterService.deleteCrawlCoverLetter(id);
            return ResponseEntity.ok(CommonResponse.success("크롤링 데이터가 삭제되었습니다."));
        } catch (CrawlCoverLetterException e) {
            log.error("크롤링 데이터 삭제 실패 - ID: {}, 오류: {}", id, e.getMessage());
            return ResponseEntity.status(e.getHttpStatus())
                    .body(CommonResponse.error(e.getErrorCode(), e.getMessage()));
        }
    }

    /**
     * 크롤링 데이터 전체 삭제
     */
    @DeleteMapping("/cover-letters")
    @Override
    public ResponseEntity<CommonResponse<Void>> deleteAllCrawlCoverLetters(@AuthenticationPrincipal UserDetails userDetails) {
        MDC.put("spanId", "crawl-delete-all-controller");

        if (userDetails == null) {
            throw new AccessDeniedException("인증되지 않은 사용자입니다.");
        }

        Member member = authService.getMemberFromUserDetails(userDetails);
        if (member.getRole() != Role.ADMIN && member.getRole() != Role.ROOT) {
            throw new AccessDeniedException("크롤링 데이터를 삭제할 권한이 없습니다. 관리자 권한이 필요합니다.");
        }

        log.info("크롤링 데이터 전체 삭제 요청 - 관리자: {}", member.getMemberId());

        crawlCoverLetterService.deleteAllCrawlCoverLetters();
        return ResponseEntity.ok(CommonResponse.success("모든 크롤링 데이터가 삭제되었습니다."));
    }
}
