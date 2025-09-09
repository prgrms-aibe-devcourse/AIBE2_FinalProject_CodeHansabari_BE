package com.cvmento.domain.coverLetter.controller;

import com.cvmento.domain.coverLetter.dto.request.CoverLetterAiRequest;
import com.cvmento.domain.coverLetter.dto.response.CoverLetterAiResponse;
import com.cvmento.domain.coverLetter.service.CoverLetterAiService;
import com.cvmento.global.common.dto.CommonResponse;
import com.cvmento.global.exception.customException.CoverLetterAiException;
import com.cvmento.global.usage.annotation.RequireTokens;
import com.cvmento.global.usage.enums.UsageType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "자소서 ai 첨삭", description = "자소서 ai 첨삭 API")
@RestController
@RequestMapping("/api/v1/cover-letters")
@RequiredArgsConstructor
public class CoverLetterAiController {
    final CoverLetterAiService coverLetterAiService;

    @Operation(
            summary = "자소서 AI 첨삭 (경력 정보 포함)",
            description = "사용자의 자소서를 AI가 첨삭하고 피드백을 제공합니다. 지원분야와 경력 정보를 바탕으로 맞춤형 첨삭을 제공합니다.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "첨삭할 자소서 내용, 지원분야, 경력정보 및 커스텀 프롬프트",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = CoverLetterAiRequest.class),
                            examples = {
                                    @ExampleObject(
                                            name = "신입 개발자 첨삭",
                                            description = "신입 개발자를 위한 자소서 첨삭 요청",
                                            value = "{\n" +
                                                    "  \"content\": \"저는 소프트웨어 개발 분야에 열정을 가지고 있으며, 대학 시절 팀 프로젝트에서 웹 애플리케이션을 구현하며 프론트엔드와 백엔드 통합 작업을 수행했습니다. 또한, 인턴십에서 실무 환경의 코드 리뷰와 협업 경험을 통해 문제 해결 능력을 키웠습니다.\",\n" +
                                                    "  \"jobField\": \"백엔드 개발자\",\n" +
                                                    "  \"experienceYears\": 0,\n" +
                                                    "  \"customPrompt\": \"신입다운 열정과 학습능력을 강조해주세요\"\n" +
                                                    "}"
                                    ),
                                    @ExampleObject(
                                            name = "경력 개발자 첨삭",
                                            description = "경력 개발자를 위한 자소서 첨삭 요청",
                                            value = "{\n" +
                                                    "  \"content\": \"저는 3년간 백엔드 개발 업무를 담당하며 대규모 트래픽을 처리하는 시스템을 설계하고 운영한 경험이 있습니다. MSA 아키텍처 도입과 성능 최적화를 통해 응답시간을 50% 개선한 바 있습니다.\",\n" +
                                                    "  \"jobField\": \"시니어 백엔드 개발자\",\n" +
                                                    "  \"experienceYears\": 3,\n" +
                                                    "  \"customPrompt\": \"기술 리더십과 성과를 수치로 강조해주세요\"\n" +
                                                    "}"
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
                                            name = "AI 첨삭 응답 예시",
                                            value = "{\n" +
                                                    "  \"success\": true,\n" +
                                                    "  \"message\": \"자소서 AI 개선이 완료되었습니다.\",\n" +
                                                    "  \"data\": {\n" +
                                                    "    \"feedback\": {\n" +
                                                    "      \"strengths\": [\n" +
                                                    "        {\n" +
                                                    "          \"description\": \"구체적인 프로젝트 경험을 명시한 점이 좋습니다\",\n" +
                                                    "          \"suggestion\": \"수치적 성과를 추가하면 더욱 효과적입니다\"\n" +
                                                    "        }\n" +
                                                    "      ],\n" +
                                                    "      \"improvements\": [\n" +
                                                    "        {\n" +
                                                    "          \"description\": \"기술적 역량에 대한 설명이 부족합니다\",\n" +
                                                    "          \"suggestion\": \"사용 기술 스택과 해결한 문제를 구체적으로 서술하세요\"\n" +
                                                    "        }\n" +
                                                    "      ],\n" +
                                                    "      \"summary\": \"전반적으로 경험이 잘 정리되어 있으나, 기술적 깊이와 성과 지표를 보완하면 더욱 완성도 높은 자소서가 될 것입니다.\"\n" +
                                                    "    },\n" +
                                                    "    \"improvedContent\": \"저는 백엔드 개발 분야에서 3년간의 실무 경험을 통해 확고한 기술적 역량을 구축해왔습니다. 특히 Spring Boot와 MySQL을 활용한 RESTful API 설계 및 구현에서 뛰어난 성과를 달성했으며, 기존 시스템의 응답 속도를 40% 개선하는 성과를 거두었습니다...\"\n" +
                                                    "  }\n" +
                                                    "}"
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "AI 처리 실패",
                            content = @Content(
                                    schema = @Schema(implementation = CommonResponse.class)
                            )
                    )
            }
    )
    @PostMapping("/ai-improve")
    @RequireTokens(UsageType.COVERLETTER_REVIEW)
    public ResponseEntity<CommonResponse<CoverLetterAiResponse>> improveCoverLetter(
            @Valid @RequestBody CoverLetterAiRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        String userEmail = userDetails.getUsername();

        try {
            CoverLetterAiResponse response = coverLetterAiService.improveCoverLetter(request, userEmail);
            return ResponseEntity.ok(
                    CommonResponse.success("자소서 AI 개선이 완료되었습니다.", response));
        } catch (CoverLetterAiException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(CommonResponse.error("AI_ANALYSIS_FAILED", e.getMessage(), true));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(CommonResponse.error("UNEXPECTED_ERROR", "처리 중 오류가 발생했습니다.", true));
        }
    }
}