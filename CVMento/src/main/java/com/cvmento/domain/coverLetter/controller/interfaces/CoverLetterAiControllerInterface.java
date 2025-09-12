package com.cvmento.domain.coverLetter.controller.interfaces;

import com.cvmento.domain.coverLetter.dto.request.CoverLetterAiRequest;
import com.cvmento.domain.coverLetter.dto.response.CoverLetterAiResponse;
import com.cvmento.global.common.dto.CommonResponse;
import com.cvmento.global.usage.annotation.RequireTokens;
import com.cvmento.global.usage.enums.UsageType;
import io.swagger.v3.oas.annotations.Operation;
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
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "자소서 AI 첨삭", description = "AI를 활용한 자소서 첨삭 및 개선 서비스 - 경력과 지원분야를 고려한 맞춤형 피드백 제공")
public interface CoverLetterAiControllerInterface {

    @Operation(
            summary = "자소서 AI 첨삭",
            description = """
                    사용자의 자소서를 AI가 분석하여 전문적인 첨삭과 피드백을 제공합니다.
                    
                    **주요 기능:**
                    - 지원분야와 경력 수준에 맞춤형 분석
                    - 구체적인 강점과 개선사항 제시
                    - 개선된 자소서 내용 생성
                    - 사용자 맞춤 요구사항 반영
                    
                    **분석 기준:**
                    - 우수 자소서 작성 기준 데이터베이스 활용
                    - 해당 분야 전문성 고려
                    - 경력 수준별 차별화된 피드백
                    """,
            security = @SecurityRequirement(name = "cookieAuth"),
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "첨삭할 자소서 내용, 지원분야, 경력정보 및 커스텀 프롬프트",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = CoverLetterAiRequest.class),
                            examples = {
                                    @ExampleObject(
                                            name = "신입 개발자 자소서",
                                            description = "신입 백엔드 개발자 지원자의 자소서 첨삭 요청",
                                            value = """
                                                    {
                                                      "content": "저는 소프트웨어 개발 분야에 열정을 가지고 있으며, 대학 시절 팀 프로젝트에서 웹 애플리케이션을 구현하며 프론트엔드와 백엔드 통합 작업을 수행했습니다. 또한, 인턴십에서 실무 환경의 코드 리뷰와 협업 경험을 통해 문제 해결 능력을 키웠습니다.",
                                                      "jobField": "백엔드 개발자",
                                                      "experienceYears": 0,
                                                      "customPrompt": "신입다운 열정과 학습능력을 강조해주세요"
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "경력 개발자 자소서",
                                            description = "3년 경력 백엔드 개발자의 자소서 첨삭 요청",
                                            value = """
                                                    {
                                                      "content": "저는 3년간 백엔드 개발 업무를 담당하며 대규모 트래픽을 처리하는 시스템을 설계하고 운영한 경험이 있습니다. MSA 아키텍처 도입과 성능 최적화를 통해 응답시간을 50% 개선한 바 있으며, 팀 내 코드 리뷰 문화를 정착시키고 신입 개발자 멘토링을 통해 팀의 전반적인 개발 역량 향상에 기여했습니다.",
                                                      "jobField": "시니어 백엔드 개발자",
                                                      "experienceYears": 3,
                                                      "customPrompt": null
                                                    }
                                                    """
                                    )
                            }
                    )
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "AI 첨삭 성공",
                            content = @Content(
                                    schema = @Schema(implementation = CommonResponse.class),
                                    examples = @ExampleObject(
                                            name = "AI 첨삭 성공 응답",
                                            value = """
                                                    {
                                                      "success": true,
                                                      "message": "자소서 AI 개선이 완료되었습니다.",
                                                      "data": {
                                                        "feedback": {
                                                          "strengths": [
                                                            {
                                                              "description": "구체적인 프로젝트 경험과 사용 기술을 명시한 점이 우수합니다",
                                                              "suggestion": "각 프로젝트에서의 본인의 역할과 기여도를 더 구체적으로 명시하면 효과적입니다"
                                                            },
                                                            {
                                                              "description": "인턴십 경험을 통한 실무 역량 습득 과정이 잘 드러납니다",
                                                              "suggestion": "협업 과정에서 겪은 구체적인 문제와 해결 방법을 추가하면 더욱 설득력이 있습니다"
                                                            }
                                                          ],
                                                          "improvements": [
                                                            {
                                                              "description": "기술적 성과에 대한 정량적 지표가 부족합니다",
                                                              "suggestion": "성능 개선 수치, 처리한 데이터 규모, 사용자 수 등 구체적인 숫자를 포함하세요"
                                                            },
                                                            {
                                                              "description": "지원하는 회사나 직무와의 연결점이 명확하지 않습니다",
                                                              "suggestion": "해당 분야에서의 비전과 성장 계획을 구체적으로 제시하세요"
                                                            }
                                                          ],
                                                          "summary": "전반적으로 기술적 경험이 체계적으로 정리되어 있으나, 정량적 성과와 직무 적합성을 보완하면 더욱 완성도 높은 자소서가 될 것입니다. 특히 신입 개발자로서의 열정과 학습 의지가 잘 드러나도록 개선했습니다."
                                                        },
                                                        "improvedContent": "저는 소프트웨어 개발 분야에 대한 확고한 열정과 지속적인 학습 의지를 바탕으로 백엔드 개발자로 성장하고자 합니다. 대학 시절 진행한 팀 프로젝트에서는 Spring Boot와 JPA를 활용하여 RESTful API를 설계하고 구현하는 역할을 담당했습니다. 특히 데이터베이스 최적화를 통해 조회 성능을 40% 향상시켰으며, 이 과정에서 인덱스 설계와 쿼리 튜닝의 중요성을 깊이 이해하게 되었습니다.\\n\\n6개월간의 인턴십에서는 실무 환경에서의 협업과 코드 품질 관리를 경험했습니다. 코드 리뷰 과정에서 클린 코드 작성법을 익혔고, Git을 활용한 버전 관리와 CI/CD 파이프라인 구축에 참여했습니다. 또한 레거시 시스템의 버그를 발견하고 수정하는 과정에서 문제 해결 능력과 분석적 사고력을 키울 수 있었습니다.\\n\\n앞으로는 클라우드 기술과 마이크로서비스 아키텍처에 대한 이해를 더욱 깊게 하여, 확장 가능하고 안정적인 시스템을 구축하는 백엔드 개발자로 성장하고 싶습니다."
                                                      },
                                                      "timestamp": "2024-01-15T14:30:00"
                                                    }
                                                    """
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "잘못된 요청 데이터 또는 유효성 검사 실패",
                            content = @Content(
                                    schema = @Schema(implementation = CommonResponse.class),
                                    examples = @ExampleObject(
                                            name = "유효성 검사 실패",
                                            value = """
                                                    {
                                                      "timestamp": "2024-01-15T14:30:00",
                                                      "status": 400,
                                                      "error": "Bad Request",
                                                      "errorCode": "VALIDATION_ERROR",
                                                      "message": "입력값이 올바르지 않습니다.",
                                                      "errors": {
                                                        "content": "자소서는 100자 이상 2000자 이하로 작성해주세요.",
                                                        "jobField": "지원분야는 필수입니다."
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
                                                      "message": "AI 첨삭 토큰이 부족합니다. 토큰을 충전해주세요.",
                                                      "errors": {
                                                        "usageType": "COVERLETTER_REVIEW",
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
                            description = "AI 처리 실패 또는 서버 내부 오류",
                            content = @Content(
                                    schema = @Schema(implementation = CommonResponse.class),
                                    examples = @ExampleObject(
                                            name = "AI 서비스 오류",
                                            value = """
                                                    {
                                                      "timestamp": "2024-01-15T14:30:00",
                                                      "status": 500,
                                                      "error": "Internal Server Error",
                                                      "errorCode": "AI_SERVICE_ERROR",
                                                      "message": "AI 분석 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.",
                                                      "errors": {}
                                                    }
                                                    """
                                    )
                            )
                    )
            }
    )
    @RequireTokens(UsageType.COVERLETTER_REVIEW)
    ResponseEntity<CommonResponse<CoverLetterAiResponse>> improveCoverLetter(
            @Valid @RequestBody CoverLetterAiRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    );
}