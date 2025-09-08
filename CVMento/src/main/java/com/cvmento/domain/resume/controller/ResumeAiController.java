package com.cvmento.domain.resume.controller;

import com.cvmento.domain.resume.dto.request.ResumeAiExperienceRequest;
import com.cvmento.domain.resume.dto.response.ResumeAiSuggestionResponse;
import com.cvmento.domain.resume.service.ResumeAiService;
import com.cvmento.global.common.dto.CommonResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "이력서 AI 제안", description = "AI 기반 이력서 작성 도우미 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/resumes")
public class ResumeAiController {

    private final ResumeAiService resumeAiService;

    @Operation(
            summary = "이력서 AI 제안 받기",
            description = "사용자가 입력한 경험 내용을 바탕으로 이력서에 추가할 항목들을 AI가 제안합니다.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "사용자의 경험 내용",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = ResumeAiExperienceRequest.class),
                            examples = @ExampleObject(
                                    value = "{\"experienceContent\" : \"Spring Boot 기반의 백엔드 개발자로, RESTful API 개발 및 시스템 최적화 경험을 보유하고 있습니다. 특히, 캐싱 전략을 통해 시스템 성능을 20% 개선하고, CI/CD 파이프라인 구축을 통해 배포 자동화에 기여했습니다 ABC 회사에서 Spring Boot를 활용한 RESTful API를 설계하고 개발했습니다. 대용량 트래픽에 대비하여 효과적인 캐싱 전략을 도입함으로써 시스템 처리량을 20% 증대시켰습니다. 또한, DevOps 환경에서 CI/CD 파이프라인 구축에 참여하여 개발 및 배포 프로세스의 효율성을 높였습니다.\"}"
                            )
                    )
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "AI 제안 생성 성공",
                            content = @Content(
                                    schema = @Schema(implementation = CommonResponse.class),
                                    examples = @ExampleObject(
                                            name = "제안 성공",
                                            value = """
                                                    {
                                                      "success": true,
                                                      "message": "이력서 AI 제안 성공",
                                                      "data": {
                                                        "suggestedSections": [
                                                          {
                                                            "sectionType": "WORK_EXPERIENCE",
                                                            "sectionTitle": "경력",
                                                            "items": [
                                                              {
                                                                "title": "ABC 회사",
                                                                "subTitle": "백엔드 개발자",
                                                                "startDate": "2022-01-01",
                                                                "endDate": "2024-12-31",
                                                                "description": "Spring Boot 기반 RESTful API 개발, 캐싱 전략으로 시스템 성능 20% 개선, CI/CD 파이프라인 구축"
                                                              }
                                                            ]
                                                          }
                                                        ]
                                                      }
                                                    }
                                                    """
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "AI 서비스 오류",
                            content = @Content(
                                    schema = @Schema(implementation = java.util.Map.class),
                                    examples = @ExampleObject(
                                            name = "AI 서비스 오류",
                                            value = """
                                                    {
                                                      "timestamp": "2025-09-05T14:30:00",
                                                      "status": 500,
                                                      "error": "Internal Server Error",
                                                      "errorCode": "RESUME_AI_SERVICE_ERROR",
                                                      "message": "AI 응답을 처리하는 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.",
                                                      "errors": {}
                                                    }
                                                    """
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "502",
                            description = "AI 서비스 연결 오류",
                            content = @Content(
                                    schema = @Schema(implementation = java.util.Map.class),
                                    examples = @ExampleObject(
                                            name = "연결 오류",
                                            value = """
                                                    {
                                                      "timestamp": "2025-09-05T14:30:00",
                                                      "status": 502,
                                                      "error": "Bad Gateway",
                                                      "errorCode": "RESUME_AI_CONNECTION_ERROR",
                                                      "message": "AI 서비스에 연결할 수 없습니다. 네트워크 상태를 확인하거나 잠시 후 다시 시도해주세요.",
                                                      "errors": {}
                                                    }
                                                    """
                                    )
                            )
                    )
            }
    )
    @PostMapping("/ai-suggest")
    public ResponseEntity<CommonResponse<ResumeAiSuggestionResponse>> getAiSuggestions(
            @Valid @RequestBody ResumeAiExperienceRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        ResumeAiSuggestionResponse response = resumeAiService.getResumeSuggestions(request, userDetails.getUsername());
        return ResponseEntity.ok(CommonResponse.success("이력서 AI 제안 성공", response));
    }
}
