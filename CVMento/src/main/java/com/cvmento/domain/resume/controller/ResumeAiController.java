package com.cvmento.domain.resume.controller;

import com.cvmento.domain.resume.dto.request.UserExperienceRequest;
import com.cvmento.domain.resume.dto.response.ResumeAiSuggestionResponse;
import com.cvmento.domain.resume.service.ResumeAiService;
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
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@Tag(name = "이력서 AI 제안", description = "사용자 경험 기반 이력서 섹션 추가 제안 API")
@RestController
@RequestMapping("/api/v1/me/resumes/ai-suggestions")
@RequiredArgsConstructor
public class ResumeAiController {

    private final ResumeAiService resumeAiService;

    /**
     * 사용자 경험 기반 이력서 섹션 추가 제안
     */
    @Operation(
            summary = "경험 기반 이력서 섹션 추가 제안",
            description = "사용자가 입력한 경험을 바탕으로 AI가 이력서에 추가할 수 있는 섹션 내용들을 제안합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "이력서 제안 생성 성공",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = ResumeAiSuggestionResponse.class),
                                    examples = @ExampleObject(
                                            name = "성공 응답 예시",
                                            value = """
                                                    {
                                                      "status": 200,
                                                      "message": "이력서 AI 제안 생성 성공",
                                                      "data": {
                                                        "suggestedResume": {
                                                          "name": "홍길동",
                                                          "email": "hong@example.com",
                                                          "careerType": "EXPERIENCED",
                                                          "fieldName": "백엔드 개발자",
                                                          "introduction": "2년간의 백엔드 개발 경험을...",
                                                          "careers": [...],
                                                          "projects": [...],
                                                          "techStacks": [...]
                                                        },
                                                        "improvementTips": ["경력을 더 구체적으로 작성하세요"],
                                                        "missingElements": ["프로젝트 성과 지표"]
                                                      }
                                                    }
                                                    """
                                    )
                            )
                    ),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청"),
                    @ApiResponse(responseCode = "500", description = "AI 서비스 오류")
            }
    )
    @PostMapping("/sections")
    public ResponseEntity<CommonResponse<ResumeAiSuggestionResponse>> suggestResumeSections(
            @Parameter(description = "사용자 경험 정보") @Valid @RequestBody UserExperienceRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        String userEmail = userDetails.getUsername();
        ResumeAiSuggestionResponse response = resumeAiService.generateResumeSuggestions(request, userEmail);

        return ResponseEntity.ok(CommonResponse.success("이력서 섹션 추가 제안 생성 성공", response));
    }
}