package com.cvmento.domain.coverLetter.controller;

import com.cvmento.domain.coverLetter.dto.request.CoverLetterAiRequest;
import com.cvmento.domain.coverLetter.dto.response.CoverLetterAiResponse;
import com.cvmento.domain.coverLetter.service.CoverLetterAiService;
import com.cvmento.global.common.dto.CommonResponse;
import com.cvmento.global.exception.customException.CoverLetterAiException;
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
            summary = "자소서 AI 첨삭 (커스텀 프롬프트 지원)",
            description = "사용자의 자소서를 AI가 첨삭하고 피드백을 제공합니다. 추가적으로 사용자가 원하는 특별한 요구사항을 customPrompt로 전달할 수 있습니다.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "첨삭할 자소서 내용 및 선택적 커스텀 프롬프트",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = CoverLetterAiRequest.class),
                            examples = {
                                    @ExampleObject(
                                            name = "기본 첨삭",
                                            description = "일반적인 자소서 첨삭 요청",
                                            value = "{\n" +
                                                    "  \"content\": \"지원한 분야 또는 직무를 선택한 이유와 본인의 관련 경험을 작성하세요.\\n\\n" +
                                                    "저는 소프트웨어 개발 분야에 열정을 가지고 있으며, 대학 시절 팀 프로젝트에서 웹 애플리케이션을 구현하며 프론트엔드와 백엔드 통합 작업을 수행했습니다.\\n\\n" +
                                                    "또한, 인턴십에서 실무 환경의 코드 리뷰와 협업 경험을 통해 문제 해결 능력을 키웠습니다.\\n\\n" +
                                                    "특히, 알고리즘 최적화와 데이터 구조 설계 경험을 바탕으로 서비스 성능 향상에 기여한 바 있으며, 사용자 피드백을 반영한 기능 개선 작업도 수행했습니다.\\n\\n" +
                                                    "이러한 경험은 제가 지원한 직무에서 실질적인 기여를 할 수 있는 탄탄한 기반이 됩니다.\",\n" +
                                                    "  \"customPrompt\": null\n" +
                                                    "}"
                                    ),
                                    @ExampleObject(
                                            name = "커스텀 프롬프트 포함",
                                            description = "특별한 요구사항이 포함된 첨삭 요청",
                                            value = "{\n" +
                                                    "  \"content\": \"지원한 분야 또는 직무를 선택한 이유와 본인의 관련 경험을 작성하세요.\\n\\n" +
                                                    "저는 소프트웨어 개발 분야에 열정을 가지고 있으며, 대학 시절 팀 프로젝트에서 웹 애플리케이션을 구현하며 프론트엔드와 백엔드 통합 작업을 수행했습니다.\\n\\n" +
                                                    "또한, 인턴십에서 실무 환경의 코드 리뷰와 협업 경험을 통해 문제 해결 능력을 키웠습니다.\",\n" +
                                                    "  \"customPrompt\": \"리더십 경험을 더 강조하고, 구체적인 성과 수치를 포함해서 작성해주세요. 또한 스타트업에 지원하는 것이므로 도전정신과 빠른 학습능력을 부각시켜 주세요.\"\n" +
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
                                    schema = @Schema(implementation = CommonResponse.class)
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