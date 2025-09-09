package com.cvmento.domain.coverLetter.controller;

import com.cvmento.domain.coverLetter.dto.response.FeatureCandidate;
import com.cvmento.domain.coverLetter.service.CoverLetterFeatureService;
import com.cvmento.global.common.dto.CommonResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cover-letter-feature")
@RequiredArgsConstructor
@Tag(name = "Cover Letter Feature", description = "자소서 특징 추출 API")
public class CoverLetterFeatureController {

    private final CoverLetterFeatureService coverLetterFeatureService;

    @PostMapping("/extract")
    @PreAuthorize("hasAnyRole('ADMIN', 'ROOT')")
    @Operation(
        summary = "크롤링된 자소서에서 특징 추출", 
        description = """
            크롤링된 자소서 데이터를 분석하여 합격 자소서의 특징 100개를 추출합니다.
            
            **처리 과정:**
            1. 크롤링된 자소서 데이터 조회
            2. 자소서를 청크로 분할 (600-1200자 단위, 15% 오버랩)
            3. 각 청크에서 LLM을 통해 특징 추출
            4. 중복 제거 및 병합
            5. 최종 100개 특징 선정 (표현력 34개, 구조 33개, 스토리 33개)
            
            **권한:** 관리자(ADMIN) 또는 최상위 관리자(ROOT)만 접근 가능
            """
    )
    @ApiResponse(
        responseCode = "200", 
        description = "특징 추출 성공",
        content = @io.swagger.v3.oas.annotations.media.Content(
            mediaType = "application/json",
            examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                value = """
                {
                  "success": true,
                  "data": [
                    {
                      "feature_category": "EXPRESSION",
                      "description": "성과나 결과를 구체적인 숫자로 표현하여 신뢰성을 높이는 기법"
                    }
                  ]
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
        description = "서버 오류 - 특징 추출 중 오류 발생"
    )
    @SecurityRequirement(name = "cookieAuth")
    public ResponseEntity<CommonResponse<List<FeatureCandidate>>> extractFeatures() {
        try {
            List<FeatureCandidate> features = coverLetterFeatureService.extractFeaturesFromCrawledData();
            return ResponseEntity.ok(CommonResponse.success("특징 추출이 완료되었습니다.", features));
        } catch (Exception e) {
            return ResponseEntity.ok(CommonResponse.error("EXTRACTION_FAILED", "특징 추출 중 오류가 발생했습니다: " + e.getMessage()));
        }
    }

    @GetMapping("/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'ROOT')")
    @Operation(
        summary = "특징 추출 상태 확인", 
        description = "현재 특징 추출 작업의 상태를 확인합니다."
    )
    @ApiResponse(responseCode = "200", description = "상태 확인 성공")
    @SecurityRequirement(name = "cookieAuth")
    public ResponseEntity<CommonResponse<String>> getStatus() {
        return ResponseEntity.ok(CommonResponse.success("특징 추출 서비스가 정상적으로 작동 중입니다.", "정상"));
    }
}
