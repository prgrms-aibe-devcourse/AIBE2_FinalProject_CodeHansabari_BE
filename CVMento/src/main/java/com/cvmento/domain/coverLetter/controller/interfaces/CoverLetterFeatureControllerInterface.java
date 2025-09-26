package com.cvmento.domain.coverLetter.controller.interfaces;

import com.cvmento.global.common.dto.CommonResponse;
import java.util.Map;
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

@Tag(name = "자소서 특징 추출", description = "자소서 특징 추출 및 조회 API. LLM으로 합격 자소서를 분석하여 핵심 특징을 추출하고, 프론트엔드에서 사용할 수 있도록 조회 기능을 제공합니다.")
public interface CoverLetterFeatureControllerInterface {

    @Operation(
            summary = "(관리자용) 자소서 특징 추출 (Sub-Backend 위임)",
            description = """
                    Sub-Backend에 자소서 특징 추출 작업을 요청합니다.
                    API는 작업을 시작시키고 즉시 응답을 반환하며, 실제 특징 추출은 백그라운드에서 비동기적으로 수행됩니다.

                    **처리 과정:**
                    1. Main-Backend API 호출
                    2. 현재 실행 중인 다른 Job이 있는지 확인 (중복 실행 방지)
                    3. Sub-Backend에 `FEATURE_EXTRACTION` Job 시작 요청
                    4. Sub-Backend는 크롤링된 자소서 데이터를 기반으로 특징을 추출하여 자체 DB의 `raw_cover_letter_feature` 테이블에 저장

                    **권한:** ADMIN 또는 ROOT만 접근 가능
                    """,
            security = @SecurityRequirement(name = "cookieAuth"),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "특징 추출 작업 시작 성공",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = CommonResponse.class),
                                    examples = @ExampleObject(
                                            name = "작업 시작 성공",
                                            value = """
                                                    {
                                                      "success": true,
                                                      "message": "자소서 특징 추출 작업이 시작되었습니다.",
                                                      "data": {
                                                        "jobId": "a1b2c3d4-e5f6-7890-1234-567890abcdef",
                                                        "task": "FEATURE_EXTRACTION",
                                                        "status": "PENDING"
                                                      },
                                                      "timestamp": "2024-01-15T14:30:00Z"
                                                    }
                                                    """
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "잘못된 요청 - 이미 다른 Job이 실행 중일 경우",
                            content = @Content(
                                    schema = @Schema(implementation = CommonResponse.class),
                                    examples = @ExampleObject(
                                            name = "이미 작업 실행 중",
                                            value = """
                                                    {
                                                      "success": false,
                                                      "errorCode": "JOB_ALREADY_ACTIVE",
                                                      "message": "현재 다른 작업이 진행 중입니다. 작업 완료 후 다시 시도해주세요.",
                                                      "timestamp": "2024-01-15T14:31:00Z"
                                                    }
                                                    """
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "권한 없음 - 관리자 권한이 필요합니다",
                            content = @Content(schema = @Schema(implementation = CommonResponse.class))
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "서버 오류 - 작업 시작 중 오류 발생",
                            content = @Content(schema = @Schema(implementation = CommonResponse.class))
                    )
            }
    )
    ResponseEntity<CommonResponse<Map<String, Object>>> extractFeatures(
            @AuthenticationPrincipal UserDetails userDetails
    );



    @Operation(
            summary = "(관리자용) 특징 중복 제거 (Sub-Backend 위임)",
            description = """
                    Sub-Backend에 Farthest-First 클러스터링 기반 특징 중복 제거 작업을 요청합니다.
                    API는 작업을 시작시키고 즉시 응답을 반환하며, 실제 중복 제거는 백그라운드에서 비동기적으로 수행됩니다.

                    **처리 과정:**
                    1. Main-Backend API 호출
                    2. 현재 실행 중인 다른 Job이 있는지 확인 (중복 실행 방지)
                    3. Sub-Backend에 `DEDUPLICATION` Job 시작 요청
                    4. Sub-Backend는 `raw_cover_letter_feature` 테이블의 데이터를 분석하여 대표 특징을 선정한 후, 그 결과를 Main-Backend DB의 `cover_letter_feature` 테이블에 저장합니다.

                    **권한:** ADMIN 또는 ROOT만 접근 가능
                    """,
            security = @SecurityRequirement(name = "cookieAuth"),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "중복 제거 작업 시작 성공",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = CommonResponse.class),
                                    examples = @ExampleObject(
                                            name = "작업 시작 성공",
                                            value = """
                                                    {
                                                      "success": true,
                                                      "message": "특징 중복제거 작업이 시작되었습니다.",
                                                      "data": {
                                                        "jobId": "b2c3d4e5-f6a7-8901-2345-67890abcdef1",
                                                        "task": "DEDUPLICATION",
                                                        "status": "PENDING"
                                                      },
                                                      "timestamp": "2024-01-15T14:30:00Z"
                                                    }
                                                    """
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "잘못된 요청 - 이미 다른 Job이 실행 중일 경우",
                            content = @Content(schema = @Schema(implementation = CommonResponse.class))
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
    ResponseEntity<CommonResponse<Map<String, Object>>> deduplicateFeatures(
            @AuthenticationPrincipal UserDetails userDetails
    );

    @Operation(
            summary = "(관리자용) 자소서 특징 추출 및 중복 제거 전체 프로세스 실행 (Sub-Backend 위임)",
            description = """
                    자소서 특징 추출부터 Farthest-First 클러스터링 기반 중복 제거까지의 전체 프로세스를 Sub-Backend에 요청합니다.
                    API는 작업을 시작시키고 즉시 응답을 반환하며, 실제 처리는 백그라운드에서 비동기적으로 수행됩니다.

                     **처리 과정:**
                    1. Main-Backend API 호출
                    2. 현재 실행 중인 다른 Job이 있는지 확인 (중복 실행 방지)
                    3. Sub-Backend에 `FEATURE_PROCESS_ALL` Job 시작 요청
                    4. Sub-Backend는 특징 추출과 중복 제거를 순차적으로 실행하고, 최종 결과를 Main-Backend DB에 저장합니다.

                    **권한:** ADMIN 또는 ROOT만 접근 가능
                    """,
            security = @SecurityRequirement(name = "cookieAuth"),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "전체 특징 처리 작업 시작 성공",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = CommonResponse.class),
                                    examples = @ExampleObject(
                                            name = "전체 처리 작업 시작",
                                            value = """
                                                    {
                                                      "success": true,
                                                      "message": "전체 특징 처리 작업이 시작되었습니다.",
                                                      "data": {
                                                        "jobId": "c3d4e5f6-a7b8-9012-3456-7890abcdef12",
                                                        "task": "FEATURE_PROCESS_ALL",
                                                        "status": "PENDING"
                                                      },
                                                      "timestamp": "2024-01-15T14:30:00Z"
                                                    }
                                                    """
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "잘못된 요청 - 이미 다른 Job이 실행 중일 경우",
                            content = @Content(schema = @Schema(implementation = CommonResponse.class))
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
            summary = "자소서 특징 전체 조회 (페이징, Sub-Backend 위임)",
            description = """
                    Sub-Backend에 저장된, 중복 제거가 완료된 최종 자소서 특징 전체를 페이징으로 조회합니다.
                    
                    **페이징 및 정렬 파라미터:**
                    - `page`: 조회할 페이지 번호 (0부터 시작)
                    - `size`: 한 페이지에 보여줄 데이터 개수
                    - `sort`: 정렬 기준. `{필드명},{ASC|DESC}` 형식으로 전달합니다. (예: `sort=createdAt,desc`)
                      - **정렬 가능 필드**: `createdAt`(생성일), `duplicateCount`(중복 횟수)
                    """,
            security = @SecurityRequirement(name = "cookieAuth"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "조회 성공",
                            content = @Content(schema = @Schema(implementation = CommonResponse.class))),
                    @ApiResponse(responseCode = "403", description = "권한 없음", content = @Content(schema = @Schema(implementation = CommonResponse.class)))
            }
    )
    ResponseEntity<CommonResponse<Page<Map<String, Object>>>> getAllFeaturesWithPagination(
            @Parameter(hidden = true) Pageable pageable,
            @AuthenticationPrincipal UserDetails userDetails
    );

    @Operation(
            summary = "카테고리별 자소서 특징 조회 (페이징, Sub-Backend 위임)",
            description = """
                    Sub-Backend에서 특정 카테고리(EXPRESSION, STRUCTURE, CONTENT)에 해당하는 최종 자소서 특징을 페이징으로 조회합니다.
                    
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
                            content = @Content(schema = @Schema(implementation = CommonResponse.class))),
                    @ApiResponse(responseCode = "403", description = "권한 없음", content = @Content(schema = @Schema(implementation = CommonResponse.class)))
            }
    )
    ResponseEntity<CommonResponse<Page<Map<String, Object>>>> getFeaturesByCategoryWithPagination(
            @Parameter(description = "조회할 특징의 카테고리", example = "EXPRESSION") @PathVariable String category,
            @Parameter(hidden = true) Pageable pageable,
            @AuthenticationPrincipal UserDetails userDetails
    );

    @Operation(
            summary = "자소서 특징 통계 조회 (Sub-Backend 위임)",
            description = "Sub-Backend에서 전체 자소서 특징의 수와 카테고리별(EXPRESSION, STRUCTURE, CONTENT) 특징의 수를 조회합니다.",
            security = @SecurityRequirement(name = "cookieAuth"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "조회 성공"),
                    @ApiResponse(responseCode = "403", description = "권한 없음", content = @Content(schema = @Schema(implementation = CommonResponse.class)))
            }
    )
    ResponseEntity<CommonResponse<Map<String, Object>>> getFeatureStatistics(
            @AuthenticationPrincipal UserDetails userDetails
    );

    @Operation(
            summary = "원본 자소서 특징 전체 조회 (페이징, Sub-Backend 위임)",
            description = """
                    Sub-Backend에서 중복 제거를 수행하기 전의 원본(Raw) 자소서 특징 전체를 페이징으로 조회합니다. (주로 관리자용)
                    
                    **페이징 및 정렬 파라미터:**
                    - `page`: 조회할 페이지 번호 (0부터 시작)
                    - `size`: 한 페이지에 보여줄 데이터 개수
                    - `sort`: 정렬 기준. `{필드명},{ASC|DESC}` 형식으로 전달합니다. (예: `sort=createdAt,desc`)
                      - **정렬 가능 필드**: `createdAt`(생성일)
                    """,
            security = @SecurityRequirement(name = "cookieAuth"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "조회 성공",
                            content = @Content(schema = @Schema(implementation = CommonResponse.class))),
                    @ApiResponse(responseCode = "403", description = "권한 없음", content = @Content(schema = @Schema(implementation = CommonResponse.class)))
            }
    )
    ResponseEntity<CommonResponse<Page<Map<String, Object>>>> getRawFeaturesPaged(
            @Parameter(hidden = true) Pageable pageable,
            @AuthenticationPrincipal UserDetails userDetails
    );

    @Operation(
            summary = "카테고리별 원본 자소서 특징 조회 (페이징, Sub-Backend 위임)",
            description = """
                    Sub-Backend에서 특정 카테고리(EXPRESSION, STRUCTURE, CONTENT)에 해당하는 원본(Raw) 자소서 특징을 페이징으로 조회합니다.
                    
                    **페이징 파라미터:**
                    - `page`: 조회할 페이지 번호 (0부터 시작)
                    - `size`: 한 페이지에 보여줄 데이터 개수
                    - `sort`: 정렬 기준. `{필드명},{ASC|DESC}` 형식으로 전달합니다.
                      - **정렬 가능 필드**: `createdAt`(생성일)
                    """,
            security = @SecurityRequirement(name = "cookieAuth"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "조회 성공",
                            content = @Content(schema = @Schema(implementation = CommonResponse.class))),
                    @ApiResponse(responseCode = "403", description = "권한 없음", content = @Content(schema = @Schema(implementation = CommonResponse.class)))
            }
    )
    ResponseEntity<CommonResponse<Page<Map<String, Object>>>> getRawFeaturesByCategoryPaged(
            @Parameter(description = "조회할 원본 특징의 카테고리", example = "EXPRESSION") @PathVariable String category,
            @Parameter(hidden = true) Pageable pageable,
            @AuthenticationPrincipal UserDetails userDetails
    );
}
