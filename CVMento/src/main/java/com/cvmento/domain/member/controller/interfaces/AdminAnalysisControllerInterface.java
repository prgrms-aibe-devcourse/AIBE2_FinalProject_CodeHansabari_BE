package com.cvmento.domain.member.controller.interfaces;

import com.cvmento.global.common.dto.CommonResponse;
import com.cvmento.global.subBackend.dto.response.StepStatusResponse;
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

import java.util.Map;

@Tag(name = "관리자 분석 기능", description = "관리자 전용 분석 시스템 상태 조회 API")
public interface AdminAnalysisControllerInterface {

    @Operation(
            summary = "분석 시스템 상태 조회 (관리자 전용)",
            description = """
                    서브 백엔드의 분석 시스템 상태를 조회합니다.
                    
                    **조회 방식:**
                    - step 파라미터 없음: 가장 최근 작업 정보 반환
                    - step=crawling: 크롤링 단계 상태만 조회
                    - step=llm-analysis: LLM 분석 단계 상태만 조회  
                    - step=deduplication: 중복제거 단계 상태만 조회
                    
                    **상태 종류:**
                    - IDLE: 대기 중
                    - RUNNING: 실행 중
                    - COMPLETED: 완료됨
                    - FAILED: 실패함
                    
                    **권한:** 관리자(ADMIN) 또는 최상위 관리자(ROOT)만 접근 가능
                    """,
            security = @SecurityRequirement(name = "cookieAuth"),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "분석 상태 조회 성공",
                            content = @Content(
                                    schema = @Schema(implementation = CommonResponse.class),
                                    examples = {
                                            @ExampleObject(
                                                    name = "최신 작업 정보 (작업 이력 있음)",
                                                    description = "step 파라미터 없이 호출 시 - 가장 최근 실행된 작업 정보",
                                                    value = """
                                                            {
                                                              "success": true,
                                                              "message": "분석 상태 조회 성공",
                                                              "data": {
                                                                "step": "LLM_ANALYSIS",
                                                                "status": "COMPLETED",
                                                                "startedAt": "2025-09-23T10:30:00",
                                                                "completedAt": "2025-09-23T11:15:00",
                                                                "createdBy": "admin@cvmento.com"
                                                              },
                                                              "timestamp": "2025-09-23T14:02:24"
                                                            }
                                                            """
                                            ),
                                            @ExampleObject(
                                                    name = "특정 단계 조회 (실행 이력 있음)",
                                                    description = "step=crawling으로 크롤링 단계만 조회한 경우",
                                                    value = """
                                                            {
                                                              "success": true,
                                                              "message": "분석 상태 조회 성공",
                                                              "data": {
                                                                "step": "CRAWLING",
                                                                "status": "COMPLETED",
                                                                "startedAt": "2025-09-23T09:00:00",
                                                                "completedAt": "2025-09-23T09:08:00",
                                                                "createdBy": "admin@cvmento.com"
                                                              },
                                                              "timestamp": "2025-09-23T14:02:24"
                                                            }
                                                            """
                                            ),
                                            @ExampleObject(
                                                    name = "특정 단계 조회 (실행 이력 없음)",
                                                    description = "해당 단계가 아직 실행된 적이 없는 경우",
                                                    value = """
                                                            {
                                                              "success": true,
                                                              "message": "분석 상태 조회 성공",
                                                              "timestamp": "2025-09-23T14:02:24"
                                                            }
                                                            """
                                            ),
                                            @ExampleObject(
                                                    name = "실패한 작업",
                                                    description = "작업이 실패한 경우",
                                                    value = """
                                                            {
                                                              "success": true,
                                                              "message": "분석 상태 조회 성공",
                                                              "data": {
                                                                "step": "CRAWLING",
                                                                "status": "FAILED",
                                                                "startedAt": "2025-09-23T08:00:00",
                                                                "completedAt": "2025-09-23T08:03:00",
                                                                "createdBy": "admin@cvmento.com"
                                                              },
                                                              "timestamp": "2025-09-23T14:02:24"
                                                            }
                                                            """
                                            )
                                    }
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "잘못된 단계명",
                            content = @Content(
                                    schema = @Schema(implementation = Map.class),
                                    examples = @ExampleObject(
                                            name = "잘못된 단계명 오류",
                                            value = """
                                {
                                  "timestamp": "2025-09-23T14:02:24",
                                  "status": 400,
                                  "error": "Bad Request",
                                  "errorCode": "INVALID_ANALYSIS_STEP",
                                  "message": "잘못된 단계명: invalid-step. 사용 가능한 값: crawling, llm-analysis, deduplication",
                                  "errors": {}
                                }
                                """
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "서버 오류 또는 서브 백엔드 통신 실패",
                            content = @Content(
                                    schema = @Schema(implementation = CommonResponse.class),
                                    examples = @ExampleObject(
                                            name = "서버 오류",
                                            value = """
                                                    {
                                                      "success": false,
                                                      "errorCode": "STATUS_ERROR",
                                                      "message": "상태 조회 실패",
                                                      "timestamp": "2025-09-23T14:02:24"
                                                    }
                                                    """
                                    )
                            )
                    )
            }
    )
    ResponseEntity<CommonResponse<StepStatusResponse>> getAnalysisStatus(
            @Parameter(
                    description = "조회할 분석 단계 (생략 시 최신 작업 정보 반환)",
                    example = "crawling",
                    schema = @Schema(
                            type = "string",
                            allowableValues = {"crawling", "llm-analysis", "deduplication"}
                    )
            )
            @RequestParam(required = false) String step,
            @AuthenticationPrincipal UserDetails userDetails
    );
}