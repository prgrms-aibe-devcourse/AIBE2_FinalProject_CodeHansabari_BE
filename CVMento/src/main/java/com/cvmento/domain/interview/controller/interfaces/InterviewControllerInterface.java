package com.cvmento.domain.interview.controller.interfaces;

import com.cvmento.domain.interview.dto.request.CustomQuestionRequest;
import com.cvmento.domain.interview.dto.response.CustomAnswerResponse;
import com.cvmento.domain.interview.dto.response.InterviewQnaListResponse;
import com.cvmento.global.common.dto.CommonResponse;
import com.cvmento.global.usage.annotation.RequireTokens;
import com.cvmento.global.usage.enums.UsageType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "AI 모의면접", description = "자소서 기반 AI 모의면접 질문/답변 생성 API")
public interface InterviewControllerInterface {

    @Operation(
            summary = "기존 면접 질문/답변 조회",
            description = "자소서에 대해 이미 생성된 면접 질문과 답변을 조회합니다. 생성된 질문이 없으면 빈 배열을 반환합니다.",
            security = @SecurityRequirement(name = "cookieAuth"),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "질문/답변 조회 성공",
                            content = @Content(
                                    schema = @Schema(implementation = CommonResponse.class),
                                    examples = {
                                            @ExampleObject(
                                                    name = "질문이 있는 경우",
                                                    value = """
                                                            {
                                                              "success": true,
                                                              "message": "면접 질문/답변 조회 성공",
                                                              "data": {
                                                                "qnaList": [
                                                                  {
                                                                    "qnaId": 1,
                                                                    "question": "프로젝트 경험에 대해 말씀해주세요.",
                                                                    "answer": "저는 대학에서 팀 프로젝트를 통해...",
                                                                    "tip": "구체적인 역할과 성과를 강조하세요.",
                                                                    "createdAt": "2025-09-05T14:30:00"
                                                                  }
                                                                ],
                                                                "totalCount": 5,
                                                                "generatedCount": 5
                                                              },
                                                              "timestamp": "2024-01-15T14:30:00"
                                                            }
                                                            """
                                            ),
                                            @ExampleObject(
                                                    name = "질문이 없는 경우",
                                                    value = """
                                                            {
                                                              "success": true,
                                                              "message": "면접 질문/답변 조회 성공",
                                                              "data": {
                                                                "qnaList": [],
                                                                "totalCount": 0,
                                                                "generatedCount": 0
                                                              },
                                                              "timestamp": "2024-01-15T14:30:00"
                                                            }
                                                            """
                                            )
                                    }
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "자소서를 찾을 수 없음",
                            content = @Content(schema = @Schema(implementation = CommonResponse.class))
                    )
            }
    )
    ResponseEntity<CommonResponse<InterviewQnaListResponse>> getInterviewQuestions(
            @Parameter(description = "자소서 ID") @PathVariable Long coverLetterId,
            @AuthenticationPrincipal UserDetails userDetails
    );

    @Operation(
            summary = "면접 질문/답변 생성",
            description = "자소서를 기반으로 면접 질문과 답변을 생성합니다. 기존 질문 수에 따라 초기 5개 또는 추가 5개를 생성합니다.",
            security = @SecurityRequirement(name = "cookieAuth"),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "질문/답변 생성 성공",
                            content = @Content(
                                    schema = @Schema(implementation = CommonResponse.class),
                                    examples = @ExampleObject(
                                            name = "생성 성공",
                                            value = """
                                                    {
                                                      "success": true,
                                                      "message": "면접 질문/답변 생성 성공",
                                                      "data": {
                                                        "qnaList": [
                                                          {
                                                            "qnaId": 6,
                                                            "question": "새로 생성된 질문",
                                                            "answer": "자소서 기반 모범 답변",
                                                            "tip": "답변 팁",
                                                            "createdAt": "2025-09-05T14:30:00"
                                                          }
                                                        ],
                                                        "totalCount": 5,
                                                        "generatedCount": 5
                                                      },
                                                      "timestamp": "2024-01-15T14:30:00"
                                                    }
                                                    """
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "409",
                            description = "질문 생성 제한 초과",
                            content = @Content(
                                    schema = @Schema(implementation = CommonResponse.class),
                                    examples = @ExampleObject(
                                            name = "생성 제한 초과",
                                            value = """
                                                    {
                                                      "timestamp": "2025-09-05T14:30:00",
                                                      "status": 409,
                                                      "error": "Conflict",
                                                      "errorCode": "INTERVIEW_LIMIT_EXCEEDED",
                                                      "message": "더 이상 질문을 생성할 수 없습니다. (최대 15개)",
                                                      "errors": {}
                                                    }
                                                    """
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "429",
                            description = "사용 한도 초과",
                            content = @Content(
                                    schema = @Schema(implementation = CommonResponse.class),
                                    examples = @ExampleObject(
                                            name = "토큰 부족",
                                            value = """
                                                    {
                                                      "timestamp": "2024-01-15T14:30:00",
                                                      "status": 429,
                                                      "error": "Too Many Requests",
                                                      "errorCode": "USAGE_LIMIT_EXCEEDED",
                                                      "message": "모의면접 토큰이 부족합니다. 토큰을 충전해주세요.",
                                                      "errors": {
                                                        "usageType": "INTERVIEW_AUTO",
                                                        "remainingTokens": "0",
                                                        "requiredTokens": "1",
                                                        "nextRefillTime": "2024-01-16T00:00:00"
                                                      }
                                                    }
                                                    """
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "서버 내부 오류",
                            content = @Content(
                                    schema = @Schema(implementation = CommonResponse.class),
                                    examples = @ExampleObject(
                                            name = "생성 실패",
                                            value = """
                                                    {
                                                      "timestamp": "2025-09-05T14:30:00",
                                                      "status": 500,
                                                      "error": "Internal Server Error",
                                                      "errorCode": "INTERVIEW_SERVICE_ERROR",
                                                      "message": "질문/답변 생성에 실패했습니다.",
                                                      "errors": {}
                                                    }
                                                    """
                                    )
                            )
                    )
            }
    )
    @RequireTokens(UsageType.INTERVIEW_AUTO)
    ResponseEntity<CommonResponse<InterviewQnaListResponse>> createInterviewQuestions(
            @Parameter(description = "자소서 ID") @PathVariable Long coverLetterId,
            @AuthenticationPrincipal UserDetails userDetails
    );

    @Operation(
            summary = "커스텀 질문 AI 답변 생성",
            description = "사용자가 직접 입력한 질문에 대해 자소서를 기반으로 한 AI 답변을 생성합니다.",
            security = @SecurityRequirement(name = "cookieAuth"),
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "사용자가 입력한 면접 질문",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = CustomQuestionRequest.class),
                            examples = @ExampleObject(
                                    name = "커스텀 질문 요청",
                                    value = """
                                            {
                                              "question": "팀워크를 발휘한 경험에 대해 설명해주세요."
                                            }
                                            """
                            )
                    )
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "답변 생성 성공",
                            content = @Content(
                                    schema = @Schema(implementation = CommonResponse.class),
                                    examples = @ExampleObject(
                                            name = "답변 생성 성공",
                                            value = """
                                                    {
                                                      "success": true,
                                                      "message": "커스텀 질문 답변 생성 성공",
                                                      "data": {
                                                        "answer": "자소서 기반 모범 답변",
                                                        "tip": "답변할 때 유의할 점과 팁"
                                                      },
                                                      "timestamp": "2024-01-15T14:30:00"
                                                    }
                                                    """
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "잘못된 요청",
                            content = @Content(
                                    schema = @Schema(implementation = CommonResponse.class),
                                    examples = @ExampleObject(
                                            name = "유효성 검증 실패",
                                            value = """
                                                    {
                                                      "timestamp": "2025-09-05T14:30:00",
                                                      "status": 400,
                                                      "error": "Bad Request",
                                                      "errorCode": "VALIDATION_ERROR",
                                                      "message": "입력값이 올바르지 않습니다.",
                                                      "errors": {
                                                        "question": "질문은 필수입니다."
                                                      }
                                                    }
                                                    """
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "429",
                            description = "사용 한도 초과",
                            content = @Content(
                                    schema = @Schema(implementation = CommonResponse.class),
                                    examples = @ExampleObject(
                                            name = "토큰 부족",
                                            value = """
                                                    {
                                                      "timestamp": "2024-01-15T14:30:00",
                                                      "status": 429,
                                                      "error": "Too Many Requests",
                                                      "errorCode": "USAGE_LIMIT_EXCEEDED",
                                                      "message": "커스텀 면접 토큰이 부족합니다. 토큰을 충전해주세요.",
                                                      "errors": {
                                                        "usageType": "INTERVIEW_CUSTOM",
                                                        "remainingTokens": "0",
                                                        "requiredTokens": "1",
                                                        "nextRefillTime": "2024-01-16T00:00:00"
                                                      }
                                                    }
                                                    """
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "서버 내부 오류",
                            content = @Content(
                                    schema = @Schema(implementation = CommonResponse.class),
                                    examples = @ExampleObject(
                                            name = "답변 생성 실패",
                                            value = """
                                                    {
                                                      "timestamp": "2025-09-05T14:30:00",
                                                      "status": 500,
                                                      "error": "Internal Server Error",
                                                      "errorCode": "INTERVIEW_SERVICE_ERROR",
                                                      "message": "커스텀 질문 답변 생성에 실패했습니다.",
                                                      "errors": {}
                                                    }
                                                    """
                                    )
                            )
                    )
            }
    )
    @RequireTokens(UsageType.INTERVIEW_CUSTOM)
    ResponseEntity<CommonResponse<CustomAnswerResponse>> createCustomAnswer(
            @Parameter(description = "자소서 ID") @PathVariable Long coverLetterId,
            @Valid @RequestBody CustomQuestionRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    );
}