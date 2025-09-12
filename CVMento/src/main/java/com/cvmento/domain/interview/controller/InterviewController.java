package com.cvmento.domain.interview.controller;

import com.cvmento.domain.interview.controller.interfaces.InterviewControllerInterface;
import com.cvmento.domain.interview.dto.request.CustomQuestionRequest;
import com.cvmento.domain.interview.dto.response.CustomAnswerResponse;
import com.cvmento.domain.interview.dto.response.InterviewQnaListResponse;
import com.cvmento.domain.interview.service.InterviewService;
import com.cvmento.global.common.dto.CommonResponse;
import com.cvmento.global.usage.annotation.RequireTokens;
import com.cvmento.global.usage.enums.UsageType;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/me/cover-letters/{coverLetterId}/interview-questions")
@RequiredArgsConstructor
public class InterviewController implements InterviewControllerInterface {

    private final InterviewService interviewService;

    /**
     * 기존 면접 질문/답변 조회
     */
    @GetMapping
    @Override
    public ResponseEntity<CommonResponse<InterviewQnaListResponse>> getInterviewQuestions(
            @PathVariable Long coverLetterId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        MDC.put("spanId", "interview-list-controller");

        String memberEmail = userDetails.getUsername();

        log.info("면접 Q&A 조회 요청 - 자소서ID: {}", coverLetterId);

        InterviewQnaListResponse response = interviewService.getExistingInterviewQna(coverLetterId, memberEmail);

        log.info("면접 Q&A 조회 완료 - 총 개수: {}, 생성된 개수: {}",
                response.totalCount(), response.generatedCount());

        return ResponseEntity.ok(CommonResponse.success("면접 질문/답변 조회 성공", response));
    }

    /**
     * 면접 질문/답변 생성
     */
    @PostMapping
    @RequireTokens(UsageType.INTERVIEW_AUTO)
    @Override
    public ResponseEntity<CommonResponse<InterviewQnaListResponse>> createInterviewQuestions(
            @PathVariable Long coverLetterId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        MDC.put("spanId", "interview-generation-controller");

        String memberEmail = userDetails.getUsername();

        log.info("면접 Q&A 생성 요청 - 자소서ID: {}", coverLetterId);

        InterviewQnaListResponse response = interviewService.createInterviewQuestions(coverLetterId, memberEmail);

        log.info("면접 Q&A 생성 완료 - 생성된 개수: {}", response.totalCount());

        return ResponseEntity.ok(CommonResponse.success("면접 질문/답변 생성 성공", response));
    }

    /**
     * 사용자 커스텀 질문에 대한 AI 답변 생성
     */
    @PostMapping("/custom-answer")
    @RequireTokens(UsageType.INTERVIEW_CUSTOM)
    @Override
    public ResponseEntity<CommonResponse<CustomAnswerResponse>> createCustomAnswer(
            @PathVariable Long coverLetterId,
            @Valid @RequestBody CustomQuestionRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        MDC.put("spanId", "custom-answer-controller");

        String memberEmail = userDetails.getUsername();

        log.info("커스텀 답변 생성 요청 - 자소서ID: {}, 질문길이: {}",
                coverLetterId, request.question() != null ? request.question().length() : 0);

        CustomAnswerResponse response = interviewService.createCustomAnswer(
                coverLetterId, memberEmail, request.question());

        log.info("커스텀 답변 생성 완료 - 답변길이: {}, 팁길이: {}",
                response.answer() != null ? response.answer().length() : 0,
                response.tip() != null ? response.tip().length() : 0);

        return ResponseEntity.ok(CommonResponse.success("커스텀 질문 답변 생성 성공", response));
    }
}