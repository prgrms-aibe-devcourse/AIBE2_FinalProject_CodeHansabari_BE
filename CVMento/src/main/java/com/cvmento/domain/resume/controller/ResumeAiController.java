package com.cvmento.domain.resume.controller;

import com.cvmento.domain.resume.dto.request.ResumeAiExperienceRequest;
import com.cvmento.domain.resume.dto.response.ResumeAiSuggestionResponse;
import com.cvmento.domain.resume.service.ResumeAiService;
import com.cvmento.global.common.dto.CommonResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
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
            )
    )
    @PostMapping("/ai-suggest")
    public ResponseEntity<CommonResponse<ResumeAiSuggestionResponse>> getAiSuggestions(
            @Valid @RequestBody ResumeAiExperienceRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        ResumeAiSuggestionResponse response = resumeAiService.getResumeSuggestions(request, userDetails.getUsername());
        return ResponseEntity.ok(CommonResponse.success("이력서 AI 제안 성공", response));
    }
}
