package com.cvmento.domain.coverLetter.controller.interfaces;

import com.cvmento.domain.coverLetter.dto.response.RawCoverLetterFeatureData;
import com.cvmento.domain.coverLetter.entity.CoverLetterFeature;
import com.cvmento.domain.coverLetter.entity.RawCoverLetterFeature;
import com.cvmento.domain.coverLetter.enums.FeaturesCategory;
import com.cvmento.global.common.dto.CommonResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import com.cvmento.domain.coverLetter.dto.response.CoverLetterFeatureData;

@Tag(name = "자소서 특징 추출", description = "자소서 특징 추출 및 조회 API. LLM으로 합격 자소서를 분석하여 핵심 특징을 추출하고, 프론트엔드에서 사용할 수 있도록 조회 기능을 제공합니다.")
public interface CoverLetterFeatureControllerInterface {

    @Operation(
            summary = "(관리자용) 크롤링 데이터에서 자소서 특징 추출",
            description = """
                    크롤링된 모든 자기소개서 데이터를 분석하여 LLM을 통해 핵심 특징을 추출합니다.
                    추출된 원본 특징은 `raw_cover_letter_feature` 테이블에 저장됩니다.
                    
                    **주요 처리 과정:**
                    1. DB에서 크롤링된 자기소개서 데이터 조회
                    2. 2개씩 배치로 그룹화하여 LLM API 요청 (효율 최적화)
                    3. 각 자소서마다 EXPRESSION, STRUCTURE, CONTENT 3가지 카테고리의 특징 추출
                    4. 추출된 특징들을 `raw_cover_letter_feature` 테이블에 저장 (중복 제거 미적용)
                    
                    **권한:** ADMIN 또는 ROOT만 접근 가능
                    """,
            security = @SecurityRequirement(name = "cookieAuth"),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "특징 추출 성공",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = CommonResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "권한 없음 - 관리자 권한이 필요합니다",
                            content = @Content(
                                    schema = @Schema(implementation = CommonResponse.class),
                                    examples = @ExampleObject(
                                            name = "권한 부족",
                                            value = """
                                                    {
                                                      "success": false,
                                                      "errorCode": "ACCESS_DENIED",
                                                      "message": "특징 추출을 실행할 권한이 없습니다. 관리자 권한이 필요합니다.",
                                                      "timestamp": "2024-01-15T14:30:00Z"
                                                    }
                                                    """
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "서버 오류 - 특징 추출 중 오류 발생",
                            content = @Content(schema = @Schema(implementation = CommonResponse.class))
                    )
            }
    )
    ResponseEntity<CommonResponse<List<RawCoverLetterFeature>>> extractFeatures(
            @AuthenticationPrincipal UserDetails userDetails
    );



    @Operation(
            summary = "(관리자용) Farthest-First 클러스터링 기반 특징 중복 제거",
            description = """
                    `raw_cover_letter_feature` 테이블에 저장된 모든 원본 특징들을 대상으로 Farthest-First 클러스터링을 수행하여 중복을 제거하고,
                    핵심 대표 특징 100개를 선정하여 `cover_letter_feature` 테이블에 저장합니다.
                    
                    **Farthest-First 클러스터링의 장점:**
                    - k-center 문제에 대한 탐욕 알고리즘으로, 의미적으로 다양한 특징을 균등하게 선택
                    - 각 카테고리별로 정확한 수의 대표 특징(클러스터) 보장 (EXPRESSION: 34개, STRUCTURE: 33개, CONTENT: 33개)
                    - 메도이드(Medoid) 보정을 통해 클러스터의 품질 향상
                    
                    **주요 처리 과정:**
                    1. `raw_cover_letter_feature` 테이블에서 모든 특징 조회
                    2. 카테고리별로 그룹화 (EXPRESSION, STRUCTURE, CONTENT)
                    3. 각 카테고리별 Farthest-First 클러스터링 수행
                    4. 초기 중복 제거(유사도 0.98 이상), 임베딩 생성 및 정규화
                    5. 대표 특징 선택 및 메도이드 보정
                    6. 최종 100개의 특징을 `cover_letter_feature` 테이블에 저장
                    
                    **권한:** ADMIN 또는 ROOT만 접근 가능
                    """,
            security = @SecurityRequirement(name = "cookieAuth"),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "중복 제거 성공",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = CommonResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "권한 없음 - 관리자 권한이 필요합니다",
                            content = @Content(schema = @Schema(implementation = CommonResponse.class))
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "서버 오류 - 중복제거 중 오류 발생",
                            content = @Content(schema = @Schema(implementation = CommonResponse.class))
                    )
            }
    )
    ResponseEntity<CommonResponse<List<CoverLetterFeature>>> deduplicateFeatures(
            @AuthenticationPrincipal UserDetails userDetails
    );
    
    @Operation(
            summary = "(관리자용) 자소서 특징 추출 및 중복 제거 전체 프로세스 실행",
            description = """
                    자소서 특징 추출부터 Farthest-First 클러스터링 기반 중복 제거까지의 전체 프로세스를 한 번에 실행합니다.
                    
                    **전체 처리 과정:**
                    1. **특징 추출**: 크롤링된 자소서에서 원본 특징을 추출하여 `raw_cover_letter_feature` 테이블에 저장합니다.
                    2. **중복 제거**: 추출된 원본 특징을 Farthest-First 클러스터링으로 분석하여 대표 특징 100개를 `cover_letter_feature` 테이블에 저장합니다.
                    3. **결과 요약 반환**: 처리된 특징의 수, 중복 제거 비율 등의 통계 정보를 반환합니다.
                    
                    **예상 처리 시간:** 약 20-25분 (추출: 15-20분, 중복 제거: 5분)
                    **권한:** ADMIN 또는 ROOT만 접근 가능
                    """,
            security = @SecurityRequirement(name = "cookieAuth"),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "전체 특징 처리 성공",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = CommonResponse.class),
                                    examples = {
                                            @ExampleObject(
                                                    name = "전체 처리 완료",
                                                    value = """
                                                            {
                                                              "success": true,
                                                              "message": "전체 특징 처리가 완료되었습니다.",
                                                              "data": {
                                                                "rawFeaturesCount": 942,
                                                                "finalFeaturesCount": 100,
                                                                "deduplicationRatio": "89.4%",
                                                                "batchSize": 2,
                                                                "totalBatches": 157,
                                                                "status": "COMPLETE",
                                                                "message": "전체 특징 처리가 완료되었습니다."
                                                              }
                                                            }
                                                            """
                                            )
                                    }
                            )
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "권한 없음 - 관리자 권한이 필요합니다",
                            content = @Content(schema = @Schema(implementation = CommonResponse.class))
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "서버 오류 - 전체 특징 처리 중 오류 발생",
                            content = @Content(schema = @Schema(implementation = CommonResponse.class))
                    )
            }
    )
    ResponseEntity<CommonResponse<Object>> extractFeaturesWithRealtimeAPI(
            @AuthenticationPrincipal UserDetails userDetails
    );

    @Operation(
            summary = "자소서 특징 전체 조회 (페이징)",
            description = """
                    중복 제거가 완료된 최종 자소서 특징 전체를 페이징으로 조회합니다.
                    프론트엔드에서는 이 API를 호출하여 사용자에게 다양한 합격 자소서의 특징들을 보여줄 수 있습니다.
                    
                    **페이징 및 정렬 파라미터:**
                    - `page`: 조회할 페이지 번호 (0부터 시작)
                    - `size`: 한 페이지에 보여줄 데이터 개수
                    - `sort`: 정렬 기준. `{필드명},{ASC|DESC}` 형식으로 전달합니다. (예: `sort=createdAt,desc`)
                      - **정렬 가능 필드**: `createdAt`(생성일), `duplicateCount`(중복 횟수)
                    """,
            security = @SecurityRequirement(name = "cookieAuth"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "조회 성공",
                            content = @Content(schema = @Schema(implementation = CommonResponse.class),
                                    examples = @ExampleObject(value = """
                                            {
                                              "success": true,
                                              "message": "요청이 성공적으로 처리되었습니다.",
                                              "data": {
                                                "content": [
                                                  {
                                                    "coverLetterFeatureId": 1,
                                                    "featuresCategory": "EXPRESSION",
                                                    "description": "문장이 간결하고 핵심 메시지가 명확함",
                                                    "duplicateCount": 9,
                                                    "representativeCoverLetterId": 101,
                                                    "createdAt": "2024-01-15T14:30:00",
                                                    "updatedAt": "2024-01-15T14:30:00"
                                                  }
                                                ],
                                                "pageable": {
                                                  "sort": { "sorted": true, "unsorted": false, "empty": false },
                                                  "offset": 0, "pageNumber": 0, "pageSize": 20, "paged": true, "unpaged": false
                                                },
                                                "totalElements": 1, "totalPages": 1, "last": true, "size": 20,
                                                "number": 0, "sort": { "sorted": true, "unsorted": false, "empty": false },
                                                "numberOfElements": 1, "first": true, "empty": false
                                              },
                                              "timestamp": "2024-01-15T14:30:00"
                                            }
                                            """
                                    ))),
                    @ApiResponse(responseCode = "403", description = "권한 없음", content = @Content(schema = @Schema(implementation = CommonResponse.class)))
            }
    )
    ResponseEntity<CommonResponse<Page<CoverLetterFeatureData>>> getAllFeaturesWithPagination(
            @Parameter(hidden = true) Pageable pageable,
            @AuthenticationPrincipal UserDetails userDetails
    );

    @Operation(
            summary = "카테고리별 자소서 특징 조회 (페이징, 중복 순 정렬)",
            description = """
                    특정 카테고리(EXPRESSION, STRUCTURE, CONTENT)에 해당하는 최종 자소서 특징을 페이징으로 조회합니다.
                    사용자가 특정 카테고리의 특징만 필터링해서 보고 싶을 때 사용합니다.
                    
                    **동작 방식:**
                    - 기본적으로 중복 횟수(`duplicateCount`)가 많은 순으로 정렬됩니다.
                    - `sort` 파라미터를 통해 다른 기준으로 정렬하는 것도 가능합니다. (예: `sort=createdAt,asc`)
                    
                    **페이징 파라미터:**
                    - `page`: 조회할 페이지 번호 (0부터 시작)
                    - `size`: 한 페이지에 보여줄 데이터 개수
                    """,
            security = @SecurityRequirement(name = "cookieAuth"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "조회 성공",
                            content = @Content(schema = @Schema(implementation = CommonResponse.class),
                                    examples = @ExampleObject(value = """
                                            {
                                              "success": true,
                                              "message": "요청이 성공적으로 처리되었습니다.",
                                              "data": {
                                                "content": [
                                                  {
                                                    "coverLetterFeatureId": 1,
                                                    "featuresCategory": "EXPRESSION",
                                                    "description": "문장이 간결하고 핵심 메시지가 명확함",
                                                    "duplicateCount": 9,
                                                    "representativeCoverLetterId": 101,
                                                    "createdAt": "2024-01-15T14:30:00",
                                                    "updatedAt": "2024-01-15T14:30:00"
                                                  }
                                                ],
                                                "pageable": {
                                                  "sort": { "sorted": true, "unsorted": false, "empty": false },
                                                  "offset": 0, "pageNumber": 0, "pageSize": 20, "paged": true, "unpaged": false
                                                },
                                                "totalElements": 1, "totalPages": 1, "last": true, "size": 20,
                                                "number": 0, "sort": { "sorted": true, "unsorted": false, "empty": false },
                                                "numberOfElements": 1, "first": true, "empty": false
                                              },
                                              "timestamp": "2024-01-15T14:30:00"
                                            }
                                            """
                                    ))),
                    @ApiResponse(responseCode = "403", description = "권한 없음", content = @Content(schema = @Schema(implementation = CommonResponse.class)))
            }
    )
    ResponseEntity<CommonResponse<Page<CoverLetterFeatureData>>> getFeaturesByCategoryWithPagination(
            @Parameter(description = "조회할 특징의 카테고리", example = "EXPRESSION") @PathVariable FeaturesCategory category,
            @Parameter(hidden = true) Pageable pageable,
            @AuthenticationPrincipal UserDetails userDetails
    );

    @Operation(
            summary = "자소서 특징 통계 조회",
            description = "전체 자소서 특징의 수와 카테고리별(EXPRESSION, STRUCTURE, CONTENT) 특징의 수를 조회합니다.",
            security = @SecurityRequirement(name = "cookieAuth"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "조회 성공"),
                    @ApiResponse(responseCode = "403", description = "권한 없음", content = @Content(schema = @Schema(implementation = CommonResponse.class)))
            }
    )
    ResponseEntity<CommonResponse<com.cvmento.domain.coverLetter.service.CoverLetterFeatureQueryService.FeatureStatistics>> getFeatureStatistics(
            @AuthenticationPrincipal UserDetails userDetails
    );

    @Operation(
            summary = "원본 자소서 특징 전체 조회 (페이징)",
            description = """
                    중복 제거를 수행하기 전의 원본(Raw) 자소서 특징 전체를 페이징으로 조회합니다. (주로 관리자용)
                    
                    **페이징 및 정렬 파라미터:**
                    - `page`: 조회할 페이지 번호 (0부터 시작)
                    - `size`: 한 페이지에 보여줄 데이터 개수
                    - `sort`: 정렬 기준. `{필드명},{ASC|DESC}` 형식으로 전달합니다. (예: `sort=createdAt,desc`)
                      - **정렬 가능 필드**: `createdAt`(생성일)
                    """,
            security = @SecurityRequirement(name = "cookieAuth"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "조회 성공",
                            content = @Content(schema = @Schema(implementation = CommonResponse.class),
                                    examples = @ExampleObject(value = """
                                            {
                                              "success": true,
                                              "message": "요청이 성공적으로 처리되었습니다.",
                                              "data": {
                                                "content": [
                                                  {
                                                    "rawCoverLetterFeatureId": 1,
                                                    "featuresCategory": "EXPRESSION",
                                                    "description": "구체적인 수치를 제시하여 성과를 명확하게 보여줌",
                                                    "coverLetterId": 101,
                                                    "createdAt": "2024-01-15T12:00:00",
                                                    "updatedAt": "2024-01-15T12:00:00"
                                                  }
                                                ],
                                                "pageable": {
                                                  "sort": { "sorted": true, "unsorted": false, "empty": false },
                                                  "offset": 0, "pageNumber": 0, "pageSize": 20, "paged": true, "unpaged": false
                                                },
                                                "totalElements": 1, "totalPages": 1, "last": true, "size": 20,
                                                "number": 0, "sort": { "sorted": true, "unsorted": false, "empty": false },
                                                "numberOfElements": 1, "first": true, "empty": false
                                              },
                                              "timestamp": "2024-01-15T14:30:00"
                                            }
                                            """
                                    ))),
                    @ApiResponse(responseCode = "403", description = "권한 없음", content = @Content(schema = @Schema(implementation = CommonResponse.class)))
            }
    )
    ResponseEntity<CommonResponse<Page<RawCoverLetterFeatureData>>> getRawFeaturesPaged(
            @Parameter(hidden = true) Pageable pageable,
            @AuthenticationPrincipal UserDetails userDetails
    );

    @Operation(
            summary = "카테고리별 원본 자소서 특징 조회 (페이징, 중복 순 정렬)",
            description = """
                    특정 카테고리(EXPRESSION, STRUCTURE, CONTENT)에 해당하는 원본(Raw) 자소서 특징을 페이징으로 조회합니다. (주로 관리자용)
                    
                    **동작 방식:**
                    - 기본적으로 중복 횟수(`duplicateCount`)가 많은 순으로 정렬됩니다.
                    - `sort` 파라미터를 통해 다른 기준으로 정렬하는 것도 가능합니다. (예: `sort=createdAt,asc`)
                    
                    **페이징 파라미터:**
                    - `page`: 조회할 페이지 번호 (0부터 시작)
                    - `size`: 한 페이지에 보여줄 데이터 개수
                    - `sort`: 정렬 기준. `{필드명},{ASC|DESC}` 형식으로 전달합니다.
                      - **정렬 가능 필드**: `duplicateCount`(중복 횟수), `createdAt`(생성일)
                    """,
            security = @SecurityRequirement(name = "cookieAuth"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "조회 성공",
                            content = @Content(schema = @Schema(implementation = CommonResponse.class),
                                    examples = @ExampleObject(value = """
                                            {
                                              "success": true,
                                              "message": "요청이 성공적으로 처리되었습니다.",
                                              "data": {
                                                "content": [
                                                  {
                                                    "rawCoverLetterFeatureId": 1,
                                                    "featuresCategory": "EXPRESSION",
                                                    "description": "구체적인 수치를 제시하여 성과를 명확하게 보여줌",
                                                    "coverLetterId": 101,
                                                    "createdAt": "2024-01-15T12:00:00",
                                                    "updatedAt": "2024-01-15T12:00:00"
                                                  }
                                                ],
                                                "pageable": {
                                                  "sort": { "sorted": true, "unsorted": false, "empty": false },
                                                  "offset": 0, "pageNumber": 0, "pageSize": 20, "paged": true, "unpaged": false
                                                },
                                                "totalElements": 1, "totalPages": 1, "last": true, "size": 20,
                                                "number": 0, "sort": { "sorted": true, "unsorted": false, "empty": false },
                                                "numberOfElements": 1, "first": true, "empty": false
                                              },
                                              "timestamp": "2024-01-15T14:30:00"
                                            }
                                            """
                                    ))),
                    @ApiResponse(responseCode = "403", description = "권한 없음", content = @Content(schema = @Schema(implementation = CommonResponse.class)))
            }
    )
    ResponseEntity<CommonResponse<Page<RawCoverLetterFeatureData>>> getRawFeaturesByCategoryPaged(
            @Parameter(description = "조회할 원본 특징의 카테고리", example = "EXPRESSION") @PathVariable FeaturesCategory category,
            @Parameter(hidden = true) Pageable pageable,
            @AuthenticationPrincipal UserDetails userDetails
    );
}
