package com.cvmento.domain.coverLetter.controller;

import com.cvmento.domain.coverLetter.controller.interfaces.CoverLetterAiControllerInterface;
import com.cvmento.domain.coverLetter.dto.request.CoverLetterAiRequest;
import com.cvmento.domain.coverLetter.dto.response.CoverLetterAiResponse;
import com.cvmento.domain.coverLetter.service.CoverLetterAiService;
import com.cvmento.global.common.dto.CommonResponse;
import com.cvmento.global.exception.customException.CoverLetterAiException;
import com.cvmento.global.usage.annotation.RequireTokens;
import com.cvmento.global.usage.enums.UsageType;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/cover-letters")
@RequiredArgsConstructor
@Slf4j
public class CoverLetterAiController implements CoverLetterAiControllerInterface {

    private final CoverLetterAiService coverLetterAiService;

    @PostMapping("/ai-improve")
    @RequireTokens(UsageType.COVERLETTER_REVIEW)
    @Override
    public ResponseEntity<CommonResponse<CoverLetterAiResponse>> improveCoverLetter(
            @Valid @RequestBody CoverLetterAiRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        MDC.put("spanId", "coverletter-ai-controller");

        String userEmail = userDetails.getUsername();

        log.info("AI 첨삭 요청 - 지원분야: {}, 경력: {}, 컨텐츠길이: {}, 커스텀프롬프트: {}",
                request.jobField(),
                request.getTotalExperience(),
                request.content() != null ? request.content().length() : 0,
                request.customPrompt() != null ? "있음" : "없음");

        try {
            CoverLetterAiResponse response = coverLetterAiService.improveCoverLetter(request, userEmail);

            log.info("AI 첨삭 완료 - 피드백항목수: {}, 개선내용길이: {}",
                    response.feedback().strengths().size() + response.feedback().improvements().size(),
                    response.improvedContent() != null ? response.improvedContent().length() : 0);

            return ResponseEntity.ok(
                    CommonResponse.success("자소서 AI 개선이 완료되었습니다.", response));

        } catch (CoverLetterAiException e) {
            log.error("AI 첨삭 비즈니스 로직 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(CommonResponse.error("AI_ANALYSIS_FAILED", e.getMessage(), true));

        } catch (Exception e) {
            log.error("AI 첨삭 처리 중 예상치 못한 오류", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(CommonResponse.error("UNEXPECTED_ERROR", "처리 중 오류가 발생했습니다.", true));
        }
    }
}