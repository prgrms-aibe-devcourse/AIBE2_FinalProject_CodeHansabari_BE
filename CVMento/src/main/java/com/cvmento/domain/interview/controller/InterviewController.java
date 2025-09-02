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
                                                            "question": "자기소개를 해주세요.",
                                                            "answer": "안녕하세요. 저는 백엔드 개발 분야에서 3년간의 실무 경험을 바탕으로 확고한 기술적 역량을 구축한 개발자입니다...",
                                                            "createdAt": "2025-09-01T10:30:00"
                                                          }
                                                        ],
                                                        "totalCount": 5,
                                                        "generatedCount": 5
                                                      }
                                                    }
                                                    """
                                    )
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
