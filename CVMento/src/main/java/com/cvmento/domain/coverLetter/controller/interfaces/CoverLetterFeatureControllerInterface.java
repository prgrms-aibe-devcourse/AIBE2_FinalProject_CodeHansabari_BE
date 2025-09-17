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

import java.util.List;
import com.cvmento.domain.coverLetter.dto.response.CoverLetterFeatureData;

@Tag(name = "자소서 특징 추출 및 관리", description = "LLM을 통한 합격 자소서 특징 분석, 추출, 중복 제거 및 조회 API")
public interface CoverLetterFeatureControllerInterface {

    @Operation(
            summary = "자소서 특징 추출 실행",
            description = """
                    크롤링된 모든 자소서 데이터를 분석하여 원본 특징(raw feature)을 추출하고 DB에 저장합니다.
                    
                    **처리 과정:**
                    1. 크롤링된 자소서 전체 조회
                    2. 기존에 추출된 원본 특징(`raw_features`) 데이터 전체 삭제
                    3. LLM API를 사용하여 자소서를 2개씩 배치로 묶어 특징 추출
                    4. 추출된 원본 특징을 `raw_features` 테이블에 저장
                    
                    **주의:** 이 작업은 모든 자소서를 대상으로 하므로 시간이 오래 걸릴 수 있습니다.
                    """,
            security = @SecurityRequirement(name = "cookieAuth"),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "특징 추출 성공",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = CommonResponse.class),
                                    examples = @ExampleObject(value = """
                                            {
                                              "success": true,
                                              "message": "특징 추출이 완료되었습니다. raw_features 테이블에 저장되었습니다.",
                                              "data": [
                                                {
                                                  "rawCoverLetterFeatureId": 1,
                                                  "featuresCategory": "EXPRESSION",
                                                  "description": "구체적인 수치를 제시하여 성과를 명확하게 보여줌",
                                                  "coverLetterId": 101,
                                                  "createdAt": "2024-01-15T12:00:00",
                                                  "updatedAt": "2024-01-15T12:00:00"
                                                }
                                              ],
                                              "timestamp": "2024-01-15T14:30:00"
                                            }
                                            """
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "권한 없음 - 관리자 권한이 필요합니다",
                            content = @Content(
                                    schema = @Schema(implementation = CommonResponse.class)
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
            summary = "전체 특징 중복 제거 실행",
            description = """
                    `raw_features` 테이블의 모든 특징을 Farthest-First 클러스터링 알고리즘으로 중복 제거하여 최종 특징(`cover_letter_features`)을 생성합니다.
                    
                    **처리 과정:**
                    1. 기존 최종 특징(`cover_letter_features`) 데이터 전체 삭제
                    2. 모든 원본 특징 조회 후 카테고리별로 그룹화
                    3. 각 카테고리별로 Farthest-First 클러스터링 수행하여 대표 특징 선정
                    4. 최종 선정된 특징들을 `cover_letter_features` 테이블에 저장
                    
                    **주의:** 이 작업은 모든 특징을 대상으로 하므로 시간이 걸릴 수 있습니다.
                    """,
            security = @SecurityRequirement(name = "cookieAuth"),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "중복 제거 성공",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = CommonResponse.class),
                                    examples = @ExampleObject(value = """
                                            {
                                              "success": true,
                                              "message": "Farthest-First 클러스터링 기반 특징 중복제거가 완료되었습니다. cover_letter_features 테이블에 저장되었습니다.",
                                              "data": [
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
                                              "timestamp": "2024-01-15T14:30:00"
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
                            description = "서버 오류 - 중복 제거 중 오류 발생",
                            content = @Content(schema = @Schema(implementation = CommonResponse.class))
                    )
            }
    )
    ResponseEntity<CommonResponse<List<CoverLetterFeature>>> deduplicateFeatures(
            @AuthenticationPrincipal UserDetails userDetails
    );


    @Operation(
            summary = "전체 특징 처리 (추출 + 중복 제거)",
            description = """
                    특징 추출부터 Farthest-First 클러스터링 중복 제거까지 전체 프로세스를 한 번에 실행합니다.
                    
                    **처리 과정:**
                    1. 크롤링된 자소서에서 특징 추출 → `raw_features` 테이블 저장
                    2. `raw_features`에서 Farthest-First 클러스터링 중복 제거 → `cover_letter_features` 테이블 저장
                    3. 처리 결과 요약 반환
                    
                    **예상 처리 시간:** 약 20-25분 (추출 15-20분 + 중복 제거 5분)
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
            summary = "[조회] 중복 제거 후 특징 페이징",
            description = "중복 제거가 완료된 최종 특징(`cover_letter_features`) 데이터를 페이징으로 조회합니다.",
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
            summary = "[조회] 카테고리별, 중복 제거 후 특징 페이징",
            description = "특정 카테고리에 대해 중복 제거가 완료된 최종 특징(`cover_letter_features`) 데이터를 페이징으로 조회합니다.",
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
            @Parameter(description = "특징 카테고리", example = "EXPRESSION") @PathVariable FeaturesCategory category,
            @Parameter(hidden = true) Pageable pageable,
            @AuthenticationPrincipal UserDetails userDetails
    );

    @Operation(
            summary = "[조회] 중복 제거 전 특징 페이징",
            description = "추출 후 정제되지 않은 원본 특징(`raw_features`)을 생성일 기준 내림차순으로 페이징 조회합니다.",
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
            summary = "[조회] 카테고리별, 중복 제거 전 특징 페이징",
            description = "특정 카테고리의 정제되지 않은 원본 특징(`raw_features`)을 페이징으로 조회합니다.",
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
            @Parameter(description = "특징 카테고리", example = "EXPRESSION") @PathVariable FeaturesCategory category,
            @Parameter(hidden = true) Pageable pageable,
            @AuthenticationPrincipal UserDetails userDetails
    );
}
