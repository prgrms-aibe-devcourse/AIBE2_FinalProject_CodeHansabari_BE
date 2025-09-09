package com.cvmento.domain.coverLetter.controller;

import com.cvmento.domain.coverLetter.dto.response.CrawlCoverLetterData;
import com.cvmento.domain.auth.service.AuthService;
import com.cvmento.domain.coverLetter.dto.response.CrawlCoverLetterResponse;
import com.cvmento.domain.coverLetter.dto.request.UpdateCrawlCoverLetterRequest;
import com.cvmento.domain.coverLetter.service.CrawlCoverLetterService;
import com.cvmento.domain.member.entity.Member;
import com.cvmento.global.common.dto.CommonResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/crawl")
@RequiredArgsConstructor
@Tag(name = "Crawl Cover Letter", description = "자소서 크롤링 API")
public class CrawlCoverLetterController {

    private final CrawlCoverLetterService crawlCoverLetterService;
    private final AuthService authService;

    @PostMapping("/cover-letters")
    @PreAuthorize("hasAnyRole('ADMIN', 'ROOT')")
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
    public ResponseEntity<CommonResponse<?>> crawlCoverLetters() {
        CrawlCoverLetterResponse response = 
            crawlCoverLetterService.crawlAndSaveCoverLetters();

        if (response.success()) {
            return ResponseEntity.ok(CommonResponse.success(response));
        } else {
            return ResponseEntity.ok(CommonResponse.error("CRAWLING_FAILED", response.message()));
        }
    }
    
    @GetMapping("/cover-letters")
    @PreAuthorize("hasAnyRole('ADMIN', 'ROOT')")
    @Operation(
        summary = "크롤링 데이터 전체 조회", 
        description = "크롤링된 모든 자소서 데이터를 조회합니다. (관리자 권한 필요)"
    )
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @ApiResponse(responseCode = "403", description = "권한 없음")
    @SecurityRequirement(name = "cookieAuth")
    public ResponseEntity<CommonResponse<List<CrawlCoverLetterData>>> getAllCrawlCoverLetters() {
        List<CrawlCoverLetterData> coverLetters = crawlCoverLetterService.getAllCrawlCoverLetters();
        return ResponseEntity.ok(CommonResponse.success(coverLetters));
    }
    
    @GetMapping("/cover-letters/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ROOT')")
    @Operation(
        summary = "크롤링 데이터 개별 조회", 
        description = "특정 ID의 크롤링된 자소서 데이터를 조회합니다. (관리자 권한 필요)"
    )
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @ApiResponse(responseCode = "403", description = "권한 없음")
    @ApiResponse(responseCode = "404", description = "데이터 없음")
    @SecurityRequirement(name = "cookieAuth")
    public ResponseEntity<CommonResponse<CrawlCoverLetterData>> getCrawlCoverLetterById(@PathVariable Long id) {
        CrawlCoverLetterData coverLetter = crawlCoverLetterService.getCrawlCoverLetterById(id);
        return ResponseEntity.ok(CommonResponse.success(coverLetter));
    }
    
    @PutMapping("/cover-letters/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ROOT')")
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
        Member member = authService.getMemberFromUserDetails(userDetails);
        CrawlCoverLetterData updatedCoverLetter = crawlCoverLetterService.updateCrawlCoverLetter(id, request, member);
        return ResponseEntity.ok(CommonResponse.success(updatedCoverLetter));
    }
    
    @DeleteMapping("/cover-letters/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ROOT')")
    @Operation(
        summary = "크롤링 데이터 개별 삭제", 
        description = "특정 ID의 크롤링된 자소서 데이터를 삭제합니다. (관리자 권한 필요)"
    )
    @ApiResponse(responseCode = "200", description = "삭제 성공")
    @ApiResponse(responseCode = "403", description = "권한 없음")
    @ApiResponse(responseCode = "404", description = "데이터 없음")
    @SecurityRequirement(name = "cookieAuth")
    public ResponseEntity<CommonResponse<Void>> deleteCrawlCoverLetter(@PathVariable Long id) {
        crawlCoverLetterService.deleteCrawlCoverLetter(id);
        return ResponseEntity.ok(CommonResponse.success("크롤링 데이터가 삭제되었습니다."));
    }
    
    @DeleteMapping("/cover-letters")
    @PreAuthorize("hasAnyRole('ADMIN', 'ROOT')")
    @Operation(
        summary = "크롤링 데이터 전체 삭제", 
        description = "크롤링된 모든 자소서 데이터를 삭제합니다. (관리자 권한 필요)"
    )
    @ApiResponse(responseCode = "200", description = "삭제 성공")
    @ApiResponse(responseCode = "403", description = "권한 없음")
    @SecurityRequirement(name = "cookieAuth")
    public ResponseEntity<CommonResponse<Void>> deleteAllCrawlCoverLetters() {
        crawlCoverLetterService.deleteAllCrawlCoverLetters();
        return ResponseEntity.ok(CommonResponse.success("모든 크롤링 데이터가 삭제되었습니다."));
    }
}
