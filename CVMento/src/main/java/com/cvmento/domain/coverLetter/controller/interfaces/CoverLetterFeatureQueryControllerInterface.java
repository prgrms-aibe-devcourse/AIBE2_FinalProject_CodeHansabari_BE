package com.cvmento.domain.coverLetter.controller.interfaces;

import com.cvmento.domain.coverLetter.dto.response.CoverLetterFeatureData;
import com.cvmento.domain.coverLetter.dto.response.CoverLetterFeaturePageResponse;
import com.cvmento.domain.coverLetter.enums.FeaturesCategory;
import com.cvmento.domain.coverLetter.service.CoverLetterFeatureQueryService;
import com.cvmento.global.common.dto.CommonResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * 추출된 특징 조회 API Swagger 문서화 인터페이스
 */
@Tag(name = "합격 자소서 특징 조회", description = "추출된 특징 조회 API - 페이징, 카테고리별 조회, 통계 정보")
public interface CoverLetterFeatureQueryControllerInterface {

    @Operation(
            summary = "모든 특징 페이징 조회",
            description = """
                    추출된 모든 특징을 페이징으로 조회합니다. (생성일 기준 내림차순)
                    
                    **기능:**
                    - 페이징을 통한 대량 데이터 효율적 조회
                    - 생성일 기준 내림차순 정렬
                    - 관리자 권한 필요
                    
                    **사용 예시:**
                    - 첫 페이지: page=0, size=20
                    - 다음 페이지: page=1, size=20
                    - 페이지 크기 최대 100개
                    """,
            security = @SecurityRequirement(name = "cookieAuth"),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "페이징 조회 성공",
                            content = @Content(
                                    schema = @Schema(implementation = CommonResponse.class),
                                    examples = @ExampleObject(
                                            name = "페이징 응답",
                                            value = """
                                                    {
                                                      "success": true,
                                                      "message": "요청이 성공적으로 처리되었습니다.",
                                                      "data": {
                                                        "content": [
                                                          {
                                                            "coverLetterFeatureId": 1,
                                                            "featuresCategory": "EXPRESSION",
                                                            "description": "구체적인 수치와 성과를 제시하여 설득력을 높임",
                                                            "duplicateCount": 15,
                                                            "representativeCoverLetterId": 123,
                                                            "createdAt": "2024-01-15T10:00:00",
                                                            "updatedAt": "2024-01-15T10:00:00"
                                                          }
                                                        ],
                                                        "totalElements": 100,
                                                        "totalPages": 5,
                                                        "currentPage": 0,
                                                        "pageSize": 20,
                                                        "hasNext": true,
                                                        "hasPrevious": false
                                                      },
                                                      "timestamp": "2024-01-15T14:30:00"
                                                    }
                                                    """
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "권한 없음",
                            content = @Content(schema = @Schema(implementation = CommonResponse.class))
                    )
            }
    )
    ResponseEntity<CommonResponse<CoverLetterFeaturePageResponse>> getAllFeaturesWithPagination(
            @Parameter(description = "페이지 번호 (0부터 시작)", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기 (최대 100)", example = "20") @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal UserDetails userDetails
    );

    @Operation(
            summary = "카테고리별 특징 페이징 조회",
            description = """
                    특정 카테고리의 특징들을 페이징으로 조회합니다. (생성일 기준 내림차순)
                    
                    **카테고리:**
                    - EXPRESSION: 표현력 관련 특징
                    - STRUCTURE: 구조 관련 특징
                    - CONTENT: 내용 관련 특징
                    
                    **기능:**
                    - 카테고리별 필터링
                    - 페이징을 통한 효율적 조회
                    - 생성일 기준 내림차순 정렬
                    """,
            security = @SecurityRequirement(name = "cookieAuth"),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "카테고리별 페이징 조회 성공",
                            content = @Content(schema = @Schema(implementation = CommonResponse.class))
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "권한 없음",
                            content = @Content(schema = @Schema(implementation = CommonResponse.class))
                    )
            }
    )
    ResponseEntity<CommonResponse<CoverLetterFeaturePageResponse>> getFeaturesByCategoryWithPagination(
            @Parameter(description = "특징 카테고리", example = "EXPRESSION") @PathVariable FeaturesCategory category,
            @Parameter(description = "페이지 번호 (0부터 시작)", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기 (최대 100)", example = "20") @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal UserDetails userDetails
    );

    @Operation(
            summary = "중복횟수 기준 특징 페이징 조회",
            description = """
                    중복횟수가 높은 특징부터 내림차순으로 페이징 조회합니다.
                    
                    **기능:**
                    - 중복횟수 기준 내림차순 정렬
                    - 인기 있는 특징 우선 조회
                    - 페이징을 통한 효율적 조회
                    """,
            security = @SecurityRequirement(name = "cookieAuth"),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "중복횟수 기준 페이징 조회 성공",
                            content = @Content(schema = @Schema(implementation = CommonResponse.class))
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "권한 없음",
                            content = @Content(schema = @Schema(implementation = CommonResponse.class))
                    )
            }
    )
    ResponseEntity<CommonResponse<CoverLetterFeaturePageResponse>> getFeaturesByDuplicateCountWithPagination(
            @Parameter(description = "페이지 번호 (0부터 시작)", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기 (최대 100)", example = "20") @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal UserDetails userDetails
    );

    @Operation(
            summary = "카테고리별 중복횟수 기준 특징 페이징 조회",
            description = """
                    특정 카테고리에서 중복횟수가 높은 특징부터 내림차순으로 페이징 조회합니다.
                    
                    **기능:**
                    - 카테고리별 필터링
                    - 중복횟수 기준 내림차순 정렬
                    - 페이징을 통한 효율적 조회
                    """,
            security = @SecurityRequirement(name = "cookieAuth"),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "카테고리별 중복횟수 기준 페이징 조회 성공",
                            content = @Content(schema = @Schema(implementation = CommonResponse.class))
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "권한 없음",
                            content = @Content(schema = @Schema(implementation = CommonResponse.class))
                    )
            }
    )
    ResponseEntity<CommonResponse<CoverLetterFeaturePageResponse>> getFeaturesByCategoryAndDuplicateCountWithPagination(
            @Parameter(description = "특징 카테고리", example = "EXPRESSION") @PathVariable FeaturesCategory category,
            @Parameter(description = "페이지 번호 (0부터 시작)", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기 (최대 100)", example = "20") @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal UserDetails userDetails
    );

    @Operation(
            summary = "모든 특징 조회 (페이징 없음)",
            description = """
                    추출된 모든 특징을 페이징 없이 조회합니다.
                    
                    **주의사항:**
                    - 대량 데이터의 경우 성능에 영향을 줄 수 있습니다.
                    - 페이징 조회를 권장합니다.
                    """,
            security = @SecurityRequirement(name = "cookieAuth"),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "모든 특징 조회 성공",
                            content = @Content(schema = @Schema(implementation = CommonResponse.class))
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "권한 없음",
                            content = @Content(schema = @Schema(implementation = CommonResponse.class))
                    )
            }
    )
    ResponseEntity<CommonResponse<List<CoverLetterFeatureData>>> getAllFeatures(
            @AuthenticationPrincipal UserDetails userDetails
    );

    @Operation(
            summary = "카테고리별 특징 조회 (페이징 없음)",
            description = """
                    특정 카테고리의 특징들을 페이징 없이 조회합니다.
                    
                    **주의사항:**
                    - 대량 데이터의 경우 성능에 영향을 줄 수 있습니다.
                    - 페이징 조회를 권장합니다.
                    """,
            security = @SecurityRequirement(name = "cookieAuth"),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "카테고리별 특징 조회 성공",
                            content = @Content(schema = @Schema(implementation = CommonResponse.class))
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "권한 없음",
                            content = @Content(schema = @Schema(implementation = CommonResponse.class))
                    )
            }
    )
    ResponseEntity<CommonResponse<List<CoverLetterFeatureData>>> getFeaturesByCategory(
            @Parameter(description = "특징 카테고리", example = "EXPRESSION") @PathVariable FeaturesCategory category,
            @AuthenticationPrincipal UserDetails userDetails
    );

    @Operation(
            summary = "특징 통계 정보 조회",
            description = """
                    추출된 특징들의 통계 정보를 조회합니다.
                    
                    **통계 정보:**
                    - 전체 특징 개수
                    - 카테고리별 특징 개수 (EXPRESSION, STRUCTURE, CONTENT)
                    
                    **사용 목적:**
                    - 데이터 현황 파악
                    - 대시보드 표시용
                    """,
            security = @SecurityRequirement(name = "cookieAuth"),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "통계 정보 조회 성공",
                            content = @Content(
                                    schema = @Schema(implementation = CommonResponse.class),
                                    examples = @ExampleObject(
                                            name = "통계 응답",
                                            value = """
                                                    {
                                                      "success": true,
                                                      "message": "요청이 성공적으로 처리되었습니다.",
                                                      "data": {
                                                        "totalCount": 100,
                                                        "expressionCount": 34,
                                                        "structureCount": 33,
                                                        "contentCount": 33
                                                      },
                                                      "timestamp": "2024-01-15T14:30:00"
                                                    }
                                                    """
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "권한 없음",
                            content = @Content(schema = @Schema(implementation = CommonResponse.class))
                    )
            }
    )
    ResponseEntity<CommonResponse<CoverLetterFeatureQueryService.FeatureStatistics>> getFeatureStatistics(
            @AuthenticationPrincipal UserDetails userDetails
    );
}
