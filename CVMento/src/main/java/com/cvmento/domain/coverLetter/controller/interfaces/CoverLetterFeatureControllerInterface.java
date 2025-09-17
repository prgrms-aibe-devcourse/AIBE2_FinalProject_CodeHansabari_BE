package com.cvmento.domain.coverLetter.controller.interfaces;

import com.cvmento.domain.coverLetter.dto.response.CoverLetterFeatureData;
import com.cvmento.domain.coverLetter.dto.response.FeatureCandidate;
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

@Tag(name = "자소서 특징 추출", description = "자소서 특징 추출 API - LLM을 통한 합격 자소서 특징 분석 및 추출")
public interface CoverLetterFeatureControllerInterface {

    @Operation(
            summary = "크롤링된 자소서에서 특징 추출 (기존 방식)",
            description = """
                    크롤링된 자소서 데이터를 분석하여 합격 자소서의 특징을 추출합니다.
                    
                    **현재 구현:**
                    - 실시간 API 사용 (2개씩 배치 처리)
                    - 자소서당 3개 특징 추출 (EXPRESSION, STRUCTURE, CONTENT)
                    - raw_features 테이블에 저장
                    
                    **처리 과정:**
                    1. 크롤링된 자소서 데이터 조회
                    2. 2개씩 배치로 그룹화 (효율성 최적화)
                    3. 각 배치에서 Gemini 실시간 API를 통해 특징 추출
                    4. raw_features 테이블에 저장 (중복 제거 없음)
                    
                    **권한:** 관리자(ADMIN) 또는 최상위 관리자(ROOT)만 접근 가능
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
            summary = "Farthest-First 클러스터링 기반 특징 중복제거",
            description = """
                    raw_features 테이블의 모든 특징을 Farthest-First 클러스터링으로 중복제거하여 final_features 테이블에 저장합니다.
                    
                    **Farthest-First 클러스터링 특징:**
                    - k-center 문제의 farthest-first(Gonzalez) 탐욕 알고리즘 사용
                    - 서로 가장 멀리 떨어진 대표 k개를 선택 후 나머지를 할당
                    - 정확한 클러스터 수 보장 (EXPRESSION: 34개, STRUCTURE: 33개, CONTENT: 33개)
                    - 의미 공간의 균등한 커버리지
                    - 메도이드 보정으로 클러스터 품질 향상
                    
                    **처리 과정:**
                    1. raw_features 테이블에서 모든 특징 조회
                    2. 카테고리별로 그룹화 (EXPRESSION, STRUCTURE, CONTENT)
                    3. 각 카테고리별로 Farthest-First 클러스터링 수행
                    4. 초기 중복 제거 (유사도 >= 0.98)
                    5. 임베딩 생성 및 정규화
                    6. Farthest-First로 대표 선택
                    7. 메도이드 보정 (1-2회)
                    8. 중복횟수와 대표 자소서 ID 계산
                    9. 최종 100개 특징 선정 및 final_features 테이블에 저장
                    
                    **권한:** 관리자(ADMIN) 또는 최상위 관리자(ROOT)만 접근 가능
                    """,
            security = @SecurityRequirement(name = "cookieAuth"),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "중복제거 성공",
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
            summary = "카테고리별 특징 중복제거",
            description = """
                    특정 카테고리의 특징만 중복제거를 수행합니다.
                    
                    **처리 과정:**
                    1. raw_features 테이블에서 지정된 카테고리의 특징만 조회
                    2. 임베딩 기반 클러스터링 수행
                    3. 중복횟수와 신뢰도 점수 계산
                    4. 해당 카테고리의 최종 특징들을 final_features 테이블에 저장
                    
                    **권한:** 관리자(ADMIN) 또는 최상위 관리자(ROOT)만 접근 가능
                    """,
            security = @SecurityRequirement(name = "cookieAuth"),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "카테고리별 중복제거 성공",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = CommonResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "잘못된 요청 - 유효하지 않은 카테고리",
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
    ResponseEntity<CommonResponse<List<CoverLetterFeature>>> deduplicateFeaturesByCategory(
            @Parameter(description = "특징 카테고리", example = "EXPRESSION") @RequestParam FeaturesCategory category,
            @AuthenticationPrincipal UserDetails userDetails
    );


    @Operation(
            summary = "전체 특징 처리 (추출 + Farthest-First 중복제거)",
            description = """
                    특징 추출부터 Farthest-First 클러스터링 중복제거까지 전체 프로세스를 한 번에 실행합니다.
                    
                    **처리 과정:**
                    1. 크롤링된 자소서에서 특징 추출 (2개씩 배치) → raw_features 테이블 저장
                    2. raw_features에서 Farthest-First 클러스터링 중복제거 → final_features 테이블 저장
                    3. 처리 결과 요약 반환
                    
                    **특징 추출:**
                    - 배치 크기: 2개씩 묶어서 처리 (효율성 최적화)
                    - 무료 계정 호환
                    - 재시도 로직: 503 오류 시 최대 3회 재시도
                    - 동적 프롬프트/스키마: 자소서 수에 따른 자동 조정
                    
                    **중복제거:**
                    - Farthest-First 클러스터링 (k-center 문제)
                    - 정확한 클러스터 수 보장 (EXPRESSION: 34개, STRUCTURE: 33개, CONTENT: 33개)
                    - 메도이드 보정으로 클러스터 품질 향상
                    
                    **예상 처리 시간:** 약 20-25분 (추출 15-20분 + 중복제거 5분)
                    **권한:** 관리자(ADMIN) 또는 최상위 관리자(ROOT)만 접근 가능
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
            summary = "Farthest-First 클러스터링 전용 중복제거",
            description = """
                    raw_features 테이블의 모든 특징을 Farthest-First 클러스터링으로만 중복제거합니다.
                    
                    **Farthest-First 클러스터링 특징:**
                    - k-center 문제의 farthest-first(Gonzalez) 탐욕 알고리즘 사용
                    - 서로 가장 멀리 떨어진 대표 k개를 선택 후 나머지를 할당
                    - 정확한 클러스터 수 보장 (EXPRESSION: 34개, STRUCTURE: 33개, CONTENT: 33개)
                    - 의미 공간의 균등한 커버리지
                    - 메도이드 보정으로 클러스터 품질 향상
                    
                    **처리 과정:**
                    1. raw_features 테이블에서 모든 특징 조회
                    2. 카테고리별로 그룹화 (EXPRESSION, STRUCTURE, CONTENT)
                    3. 각 카테고리별로 Farthest-First 클러스터링 수행
                    4. 초기 중복 제거 (유사도 >= 0.98)
                    5. 임베딩 생성 및 정규화
                    6. Farthest-First로 대표 선택
                    7. 메도이드 보정 (1-2회)
                    8. 중복횟수와 대표 자소서 ID 계산
                    9. 최종 100개 특징 선정 및 final_features 테이블에 저장
                    
                    **권한:** 관리자(ADMIN) 또는 최상위 관리자(ROOT)만 접근 가능
                    """,
            security = @SecurityRequirement(name = "cookieAuth"),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Farthest-First 클러스터링 중복제거 성공",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = CommonResponse.class),
                                    examples = @ExampleObject(
                                            name = "Farthest-First 클러스터링 성공",
                                            value = """
                                                    {
                                                      "success": true,
                                                      "message": "Farthest-First 클러스터링 기반 중복제거가 완료되었습니다. 942개 → 100개 클러스터로 압축되었습니다.",
                                                      "data": [
                                                        {
                                                          "coverLetterFeatureId": 1,
                                                          "featuresCategory": "EXPRESSION",
                                                          "description": "문장이 간결하고 핵심 메시지가 명확함",
                                                          "duplicateCount": 9,
                                                          "representativeCoverLetterId": 1,
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
                            description = "서버 오류 - Farthest-First 클러스터링 중 오류 발생",
                            content = @Content(schema = @Schema(implementation = CommonResponse.class))
                    )
            }
    )
    ResponseEntity<CommonResponse<List<CoverLetterFeature>>> deduplicateFeaturesWithFarthestFirst(
            @AuthenticationPrincipal UserDetails userDetails
    );

    @Operation(
            summary = "Raw 특징 페이징 조회",
            description = "추출 후 정제되지 않은 원본(raw) 특징을 생성일 기준 내림차순으로 페이징 조회합니다.",
            security = @SecurityRequirement(name = "cookieAuth"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = CommonResponse.class))),
                    @ApiResponse(responseCode = "403", description = "권한 없음", content = @Content(schema = @Schema(implementation = CommonResponse.class)))
            }
    )
    ResponseEntity<CommonResponse<Page<RawCoverLetterFeatureData>>> getRawFeaturesPaged(
            @Parameter(hidden = true) Pageable pageable,
            @AuthenticationPrincipal UserDetails userDetails
    );

    @Operation(
            summary = "카테고리별 Raw 특징 페이징 조회",
            description = "특정 카테고리(EXPRESSION, STRUCTURE, CONTENT)의 원본(raw) 특징을 페이징 조회합니다.",
            security = @SecurityRequirement(name = "cookieAuth"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = CommonResponse.class))),
                    @ApiResponse(responseCode = "403", description = "권한 없음", content = @Content(schema = @Schema(implementation = CommonResponse.class)))
            }
    )
    ResponseEntity<CommonResponse<Page<RawCoverLetterFeatureData>>> getRawFeaturesByCategoryPaged(
            @Parameter(description = "특징 카테고리", example = "EXPRESSION") @PathVariable FeaturesCategory category,
            @Parameter(hidden = true) Pageable pageable,
            @AuthenticationPrincipal UserDetails userDetails
    );
}
