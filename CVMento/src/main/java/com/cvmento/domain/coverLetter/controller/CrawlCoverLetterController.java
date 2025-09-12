package com.cvmento.domain.coverLetter.controller;

import com.cvmento.domain.auth.service.AuthService;
import com.cvmento.domain.coverLetter.controller.interfaces.CrawlCoverLetterControllerInterface;
import com.cvmento.domain.coverLetter.dto.request.UpdateCrawlCoverLetterRequest;
import com.cvmento.domain.coverLetter.dto.response.CrawlCoverLetterData;
import com.cvmento.domain.auth.service.AuthService;
import com.cvmento.domain.coverLetter.dto.response.CrawlCoverLetterResponse;
import com.cvmento.domain.coverLetter.service.CrawlCoverLetterService;
import com.cvmento.domain.member.entity.Member;
import com.cvmento.global.common.dto.CommonResponse;
import com.cvmento.global.exception.CrawlCoverLetterException;
import com.cvmento.domain.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
    @Operation(
        summary = "자소서 크롤링 실행", 
        description = """
            Linkareer API를 호출하여 IT 직무 합격 자소서를 크롤링하고 DB에 저장합니다.
            
            **크롤링 대상:**
            - 사이트: Linkareer (https://api.linkareer.com/graphql)
            - 직무: IT
            - 상태: PUBLISHED (공개된 자소서)
            - 정렬: 합격일 기준 내림차순
            - 페이지 크기: 314개
            
            **처리 과정:**
            1. 기존 크롤링 데이터 삭제
            2. Linkareer API 호출
            3. 응답에서 content 필드 추출
            4. DB에 저장
            
            **권한:** 관리자(ADMIN) 또는 최상위 관리자(ROOT)만 접근 가능
            """
    )
    @ApiResponse(
        responseCode = "200", 
        description = "크롤링 성공",
        content = @io.swagger.v3.oas.annotations.media.Content(
            mediaType = "application/json",
            examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                value = """
                {
                  "success": true,
                  "data": {
                    "success": true,
                    "message": "자소서 크롤링이 완료되었습니다.",
                    "crawledCount": 150
                  }
                }
                """
            )
        )
    )
    @ApiResponse(
        responseCode = "403", 
        description = "권한 없음 - 관리자 권한이 필요합니다"
    )
    @ApiResponse(
        responseCode = "500", 
        description = "서버 오류 - 크롤링 중 오류 발생"
    )
    @SecurityRequirement(name = "cookieAuth")
    public ResponseEntity<CommonResponse<?>> crawlCoverLetters(@AuthenticationPrincipal UserDetails userDetails) {
        // 관리자 권한 체크
        if (userDetails == null) {
            throw new AccessDeniedException("인증되지 않은 사용자입니다.");
        }
        
        Member member = authService.getMemberFromUserDetails(userDetails);
        if (member.getRole() != Role.ADMIN && member.getRole() != Role.ROOT) {
            throw new AccessDeniedException("크롤링을 실행할 권한이 없습니다. 관리자 권한이 필요합니다.");
        }

        try {
            CrawlCoverLetterResponse response = 
                crawlCoverLetterService.crawlAndSaveCoverLetters();

            if (response.success()) {
                return ResponseEntity.ok(CommonResponse.success(response));
            } else {
                return ResponseEntity.ok(CommonResponse.error("CRAWLING_FAILED", response.message()));
            }

        } catch (Exception e) {
            return ResponseEntity.ok(CommonResponse.error("CRAWLING_ERROR", "크롤링 중 오류가 발생했습니다: " + e.getMessage()));
        }
    }

    /**
     * 크롤링 데이터 전체 조회
     */
    @GetMapping("/cover-letters")
    @Operation(
        summary = "크롤링 데이터 전체 조회", 
        description = "크롤링된 모든 자소서 데이터를 조회합니다. (관리자 권한 필요)"
    )
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @ApiResponse(responseCode = "403", description = "권한 없음")
    @SecurityRequirement(name = "cookieAuth")
    public ResponseEntity<CommonResponse<List<CrawlCoverLetterData>>> getAllCrawlCoverLetters(@AuthenticationPrincipal UserDetails userDetails) {
        // 관리자 권한 체크
        if (userDetails == null) {
            throw new AccessDeniedException("인증되지 않은 사용자입니다.");
        }
        
        Member member = authService.getMemberFromUserDetails(userDetails);
        if (member.getRole() != Role.ADMIN && member.getRole() != Role.ROOT) {
            throw new AccessDeniedException("크롤링 데이터를 조회할 권한이 없습니다. 관리자 권한이 필요합니다.");
        }

        List<CrawlCoverLetterData> coverLetters = crawlCoverLetterService.getAllCrawlCoverLetters();

        log.info("크롤링 데이터 조회 완료 - 총 개수: {}", coverLetters.size());
        return ResponseEntity.ok(CommonResponse.success(coverLetters));
    }

    /**
     * 크롤링 데이터 단건 조회
     */
    @GetMapping("/cover-letters/{id}")
    @Operation(
        summary = "크롤링 데이터 개별 조회", 
        description = "특정 ID의 크롤링된 자소서 데이터를 조회합니다. (관리자 권한 필요)"
    )
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @ApiResponse(responseCode = "403", description = "권한 없음")
    @ApiResponse(responseCode = "404", description = "데이터 없음")
    @SecurityRequirement(name = "cookieAuth")
    public ResponseEntity<CommonResponse<CrawlCoverLetterData>> getCrawlCoverLetterById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        // 관리자 권한 체크
        if (userDetails == null) {
            throw new AccessDeniedException("인증되지 않은 사용자입니다.");
        }
        
        Member member = authService.getMemberFromUserDetails(userDetails);
        if (member.getRole() != Role.ADMIN && member.getRole() != Role.ROOT) {
            throw new AccessDeniedException("크롤링 데이터를 조회할 권한이 없습니다. 관리자 권한이 필요합니다.");
        }

        try {
            CrawlCoverLetterData coverLetter = crawlCoverLetterService.getCrawlCoverLetterById(id);
            return ResponseEntity.ok(CommonResponse.success(coverLetter));
        } catch (CrawlCoverLetterException e) {
            return ResponseEntity.status(e.getHttpStatus())
                    .body(CommonResponse.error(e.getErrorCode(), e.getMessage()));
        }
    }

    /**
     * 크롤링 데이터 수정
     */
    @PutMapping("/cover-letters/{id}")
    @Operation(
        summary = "크롤링 데이터 수정", 
        description = "특정 ID의 크롤링된 자소서 데이터를 수정합니다. (관리자 권한 필요)"
    )
    @ApiResponse(responseCode = "200", description = "수정 성공")
    @ApiResponse(responseCode = "403", description = "권한 없음")
    @ApiResponse(responseCode = "404", description = "데이터 없음")
    @SecurityRequirement(name = "cookieAuth")
    public ResponseEntity<CommonResponse<CrawlCoverLetterData>> updateCrawlCoverLetter(
            @PathVariable Long id,
            @RequestBody UpdateCrawlCoverLetterRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        // 관리자 권한 체크
        if (userDetails == null) {
            throw new AccessDeniedException("인증되지 않은 사용자입니다.");
        }
        
        Member member = authService.getMemberFromUserDetails(userDetails);
        if (member.getRole() != Role.ADMIN && member.getRole() != Role.ROOT) {
            throw new AccessDeniedException("크롤링 데이터를 수정할 권한이 없습니다. 관리자 권한이 필요합니다.");
        }

        try {
            CrawlCoverLetterData updatedCoverLetter = crawlCoverLetterService.updateCrawlCoverLetter(id, request, member);
            return ResponseEntity.ok(CommonResponse.success(updatedCoverLetter));
        } catch (CrawlCoverLetterException e) {
            return ResponseEntity.status(e.getHttpStatus())
                    .body(CommonResponse.error(e.getErrorCode(), e.getMessage()));
        }
    }