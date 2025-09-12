package com.cvmento.domain.coverLetter.controller.interfaces;

import com.cvmento.domain.coverLetter.dto.response.FeatureCandidate;
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
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Tag(name = "자소서 특징 추출", description = "자소서 특징 추출 API - LLM을 통한 합격 자소서 특징 분석 및 추출")
public interface CoverLetterFeatureControllerInterface {

    @Operation(
            summary = "크롤링된 자소서에서 특징 추출",
            description = """
                    크롤링된 자소서 데이터를 분석하여 합격 자소서의 특징 100개를 추출합니다.
                    
                    **처리 과정:**
                    1. 크롤링된 자소서 데이터 조회
                    2. 자소서들을 배치 단위로 그룹화 (토큰 제한 고려하여 동적 조정)
                    3. 각 배치에서 LLM을 통해 특징 추출 (자소서당 3개: 표현력/구조/스토리)
                    4. 중복 제거 및 병합 (의미 기반 유사도 계산)
                    5. 최종 100개 특징 선정 (표현력 34개, 구조 33개, 스토리 33개)
                    
                    **권한:** 관리자(ADMIN) 또는 최상위 관리자(ROOT)만 접근 가능
                    """,
            security = @SecurityRequirement(name = "cookieAuth"),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "특징 추출 성공",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = CommonResponse.class),
                                    examples = @ExampleObject(
                                            name = "특징 추출 성공 응답",
                                            value = """
                                                    {
                                                      "success": true,
                                                      "message": "특징 추출이 완료되었습니다.",
                                                      "data": [
                                                        {
                                                          "feature_category": "EXPRESSION",
                                                          "description": "성과나 결과를 구체적인 숫자로 표현하여 신뢰성을 높이는 기법"
                                                        },
                                                        {
                                                          "feature_category": "STRUCTURE",
                                                          "description": "상황-행동-결과의 3단 구조로 경험을 체계적으로 서술하는 패턴"
                                                        },
                                                        {
                                                          "feature_category": "CONTENT",
                                                          "description": "동아리 활동을 통한 리더십 경험을 구체적인 사례로 제시하는 방식"
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
                                    schema = @Schema(implementation = CommonResponse.class),
                                    examples = @ExampleObject(
                                            name = "권한 부족",
                                            value = """
                                                    {
                                                      "success": false,
                                                      "errorCode": "ACCESS_DENIED",
                                                      "message": "특징 추출을 실행할 권한이 없습니다. 관리자 권한이 필요합니다.",
                                                      "timestamp": "2024-01-15T14:30:00"
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
    ResponseEntity<CommonResponse<List<FeatureCandidate>>> extractFeatures(
            @AuthenticationPrincipal UserDetails userDetails
    );

    @Operation(
            summary = "테스트용: 단일 자소서 특징 추출",
            description = """
                    **테스트용 API** - 특정 자소서 하나에서만 특징을 추출합니다.
                    
                    **용도:**
                    - LLM 특징 추출 기능 테스트
                    - 프롬프트 및 응답 형식 확인
                    - 개별 자소서 분석 결과 확인
                    
                    **처리 과정:**
                    1. 지정된 ID의 자소서 조회
                    2. 자소서 전체를 한 번에 LLM에 전송
                    3. LLM에서 특징 추출 (최대 3개: 표현력/구조/스토리)
                    4. 추출된 특징 반환
                    
                    **주의사항:**
                    - DB에 저장되지 않음 (테스트용)
                    - 청킹 없이 자소서 전체를 한 번에 처리
                    - API 호출 1회만 발생 (토큰 절약)
                    
                    **권한:** 관리자(ADMIN) 또는 최상위 관리자(ROOT)만 접근 가능
                    """,
            security = @SecurityRequirement(name = "cookieAuth"),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "테스트용 특징 추출 성공",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = CommonResponse.class),
                                    examples = @ExampleObject(
                                            name = "테스트용 특징 추출 성공 응답",
                                            value = """
                                                    {
                                                      "success": true,
                                                      "message": "테스트용 특징 추출이 완료되었습니다.",
                                                      "data": [
                                                        {
                                                          "feature_category": "EXPRESSION",
                                                          "description": "성과를 구체적인 숫자(95%, 3개월)로 표현하여 신뢰성을 높이는 기법"
                                                        },
                                                        {
                                                          "feature_category": "STRUCTURE",
                                                          "description": "상황-행동-결과의 3단 구조로 경험을 체계적으로 서술하는 패턴"
                                                        },
                                                        {
                                                          "feature_category": "CONTENT",
                                                          "description": "동아리 활동을 통한 리더십 경험을 구체적인 사례로 제시하는 방식"
                                                        }
                                                      ],
                                                      "timestamp": "2024-01-15T14:30:00"
                                                    }
                                                    """
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "잘못된 요청 - 자소서 ID가 유효하지 않습니다",
                            content = @Content(schema = @Schema(implementation = CommonResponse.class))
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "권한 없음 - 관리자 권한이 필요합니다",
                            content = @Content(schema = @Schema(implementation = CommonResponse.class))
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "자소서를 찾을 수 없음",
                            content = @Content(schema = @Schema(implementation = CommonResponse.class))
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "서버 오류 - 특징 추출 중 오류 발생",
                            content = @Content(schema = @Schema(implementation = CommonResponse.class))
                    )
            }
    )
    ResponseEntity<CommonResponse<List<FeatureCandidate>>> extractFeaturesFromSingle(
            @Parameter(description = "특징을 추출할 자소서 ID") @RequestParam Long essayId,
            @AuthenticationPrincipal UserDetails userDetails
    );
}
