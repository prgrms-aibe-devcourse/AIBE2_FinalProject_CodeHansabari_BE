package com.cvmento.domain.coverLetter.controller;

import com.cvmento.domain.coverLetter.service.CrawlCoverLetterService;
import com.cvmento.domain.member.entity.Member;
import com.cvmento.domain.member.enums.Role;
import com.cvmento.global.common.dto.CommonResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/crawl")
@RequiredArgsConstructor
@Tag(name = "Crawl Cover Letter", description = "자소서 크롤링 API")
public class CrawlCoverLetterController {

    private final CrawlCoverLetterService crawlCoverLetterService;

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
    public ResponseEntity<CommonResponse<?>> crawlCoverLetters(@AuthenticationPrincipal Member member) {
        // 관리자 권한 체크
        if (member == null || (member.getRole() != Role.ADMIN && member.getRole() != Role.ROOT)) {
            throw new AccessDeniedException("크롤링을 실행할 권한이 없습니다. 관리자 권한이 필요합니다.");
        }

        try {
            CrawlCoverLetterService.CrawlCoverLetterResponse response = 
                crawlCoverLetterService.crawlAndSaveCoverLetters();

            if (response.isSuccess()) {
                return ResponseEntity.ok(CommonResponse.success(response));
            } else {
                return ResponseEntity.ok(CommonResponse.error("CRAWLING_FAILED", response.getMessage()));
            }

        } catch (Exception e) {
            return ResponseEntity.ok(CommonResponse.error("CRAWLING_ERROR", "크롤링 중 오류가 발생했습니다: " + e.getMessage()));
        }
    }
}
