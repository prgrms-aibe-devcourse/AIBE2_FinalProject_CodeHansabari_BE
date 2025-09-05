package com.cvmento.domain.interview.controller;

import com.cvmento.domain.interview.dto.response.InterviewQnaListResponse;
import com.cvmento.domain.interview.service.InterviewService;
import com.cvmento.global.common.dto.CommonResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Tag(name = "ai 모의면접", description = "자소서 기반 ai 모의면접 질문/답변 생성 API")
@RestController
@RequestMapping("/api/v1/me/cover-letters/{coverLetterId}/interview-questions")
@RequiredArgsConstructor
public class InterviewController {

    private final InterviewService interviewService;

    /**
     * 자소서 선택이후 ai 모의면접 질문 생성 (이미 생성된 질문이 있으면 재생성하지 않고 이전 내용 불러옴)
     */
    @Operation(
            summary = "AI 예상 질문/답변 생성 또는 조회",
            description = "자소서를 기반으로 AI가 예상 면접 질문과 모범 답변을 함께 생성합니다. 이미 생성된 질문이 있으면 기존 데이터를 조회합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "질문/답변 생성 또는 조회 성공",
                            content = @Content(
                                    schema = @Schema(implementation = CommonResponse.class),
                                    examples = @ExampleObject(
                                            name = "질문/답변 목록 응답",
                                            value = """
                                                    {
                                                      "success": true,
                                                      "message": "모의면접 질문/답변 조회 성공",
                                                      "data": {
                                                        "qnaList": [
                                                          {
                                                            "qnaId": 1,
                                                            "question": "프로젝트에서 사용한 Spring Boot의 주요 기능과 그 이유에 대해 설명해주세요.",
                                                            "answer": "저는 프로젝트에서 Spring Boot의 Auto Configuration을 활용하여 개발 효율성을 높였습니다. 특히 JPA와 Security 설정을 자동화하여 초기 설정 시간을 단축했고, Actuator를 통해 애플리케이션 모니터링을 구현했습니다. 또한 Profile을 활용하여 개발/운영 환경을 분리하여 배포 프로세스를 개선했습니다.",
                                                            "createdAt": "2025-09-01T10:30:00"
                                                          },
                                                          {
                                                            "qnaId": 2,
                                                            "question": "팀 프로젝트에서 발생한 기술적 문제를 어떻게 해결했는지 구체적인 사례를 들어 설명해주세요.",
                                                            "answer": "팀 프로젝트에서 동시성 문제로 인한 데이터 일관성 이슈가 발생했을 때, 낙관적 락(Optimistic Lock)과 비관적 락(Pessimistic Lock)을 상황에 맞게 적용했습니다. 읽기가 많은 경우는 낙관적 락을, 쓰기 경합이 심한 경우는 비관적 락을 사용하여 성능과 일관성을 모두 확보했습니다.",
                                                            "createdAt": "2025-09-01T10:31:00"
                                                          },
                                                          {
                                                            "qnaId": 3,
                                                            "question": "현재 기술 스택을 선택한 이유와 향후 학습 계획에 대해 말씀해주세요.",
                                                            "answer": "Java와 Spring을 선택한 이유는 안정성과 확장성, 그리고 풍부한 생태계 때문입니다. 대용량 트래픽 처리에 적합하고 엔터프라이즈 환경에서 검증된 기술이기 때문입니다. 앞으로는 MSA 아키텍처와 Kubernetes, 그리고 실시간 처리를 위한 Kafka 등을 학습하여 시스템 설계 역량을 키우고 싶습니다.",
                                                            "createdAt": "2025-09-01T10:32:00"
                                                          },
                                                          {
                                                            "qnaId": 4,
                                                            "question": "코드 리뷰나 협업 과정에서 어려웠던 점과 이를 개선한 경험이 있다면 설명해주세요.",
                                                            "answer": "초기에는 코드 리뷰에서 개인적인 의견 충돌이 있었습니다. 이를 해결하기 위해 팀 내에서 코딩 컨벤션과 리뷰 가이드라인을 정립했고, 'Why'에 집중하는 건설적인 피드백 문화를 만들었습니다. 그 결과 코드 품질이 향상되고 팀원 간 기술적 성장도 이룰 수 있었습니다.",
                                                            "createdAt": "2025-09-01T10:33:00"
                                                          },
                                                          {
                                                            "qnaId": 5,
                                                            "question": "이 회사에 입사하게 된다면 어떤 기여를 할 수 있을지, 그리고 어떤 성장을 기대하는지 말씀해주세요.",
                                                            "answer": "제가 가진 백엔드 개발 경험과 문제 해결 능력을 바탕으로 안정적이고 확장 가능한 서비스 개발에 기여하고 싶습니다. 특히 성능 최적화와 코드 품질 향상에 집중하여 팀의 개발 생산성을 높이는 역할을 하고 싶습니다. 또한 회사의 기술적 도전과 함께 성장하면서 시니어 개발자로서의 역량을 키워나가고 싶습니다.",
                                                            "createdAt": "2025-09-01T10:34:00"
                                                          }
                                                        ],
                                                        "totalCount": 5,
                                                        "generatedCount": 5
                                                      }
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
                                            name = "자소서 없음",
                                            value = """
                                                    {
                                                      "success": false,
                                                      "message": "자소서를 찾을 수 없습니다.",
                                                      "data": null
                                                    }
                                                    """
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "서버 내부 오류",
                            content = @Content(
                                    schema = @Schema(implementation = Map.class),
                                    examples = {
                                            @ExampleObject(
                                                    name = "AI 생성 실패",
                                                    value = """
                                                            {
                                                              "timestamp": "2025-09-01T10:30:00",
                                                              "status": 500,
                                                              "error": "Internal Server Error",
                                                              "errorCode": "INTERVIEW_SERVICE_ERROR",
                                                              "message": "면접 질문/답변 생성에 실패했습니다.",
                                                              "errors": {}
                                                            }
                                                            """
                                            ),
                                            @ExampleObject(
                                                    name = "LLM API 호출 실패",
                                                    value = """
                                                            {
                                                              "timestamp": "2025-09-01T10:30:00",
                                                              "status": 500,
                                                              "error": "Internal Server Error",
                                                              "errorCode": "INTERVIEW_SERVICE_ERROR",
                                                              "message": "Interview LLM API 호출 실패",
                                                              "errors": {}
                                                            }
                                                            """
                                            ),
                                            @ExampleObject(
                                                    name = "응답 파싱 실패",
                                                    value = """
                                                            {
                                                              "timestamp": "2025-09-01T10:30:00",
                                                              "status": 500,
                                                              "error": "Internal Server Error",
                                                              "errorCode": "INTERVIEW_SERVICE_ERROR",
                                                              "message": "LLM 응답 파싱에 실패했습니다.",
                                                              "errors": {}
                                                            }
                                                            """
                                            )
                                    }
                            )
                    )
            }
    )
    @GetMapping
    public ResponseEntity<CommonResponse<InterviewQnaListResponse>> getInterviewQna(
            @Parameter(description = "자소서 ID") @PathVariable Long coverLetterId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        String userEmail = userDetails.getUsername();
        InterviewQnaListResponse response = interviewService.generateOrGetInterviewQna(coverLetterId, userEmail);

        return ResponseEntity.ok(CommonResponse.success("모의면접 질문/답변 조회 성공", response));
    }

    /**
     * ai 모의면접 질문 추가 5개 생성 ( 최대 2번까지 가능, QuestionSourceType이 GENERATED인 질문 16개 이상 생성 불가 )
     */

    /**
     * 사용자 질문에 따른 ai 답변 생성 ( 답변은 1개이며, 갯수 상관없이 답변 )
     */

}