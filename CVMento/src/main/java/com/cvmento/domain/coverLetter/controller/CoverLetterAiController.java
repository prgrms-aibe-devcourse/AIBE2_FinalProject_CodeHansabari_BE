package com.cvmento.domain.coverLetter.controller;

import com.cvmento.domain.coverLetter.controller.interfaces.CoverLetterAiControllerInterface;
import com.cvmento.domain.coverLetter.dto.request.CoverLetterAiRequest;
import com.cvmento.domain.coverLetter.dto.response.CoverLetterAiResponse;
import com.cvmento.domain.coverLetter.service.CoverLetterAiService;
import com.cvmento.global.common.dto.CommonResponse;
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

/**
 * 자소서 AI 개선 컨트롤러.
 */
@RestController
@RequestMapping("/api/v1/cover-letters")
@RequiredArgsConstructor
@Slf4j
public class CoverLetterAiController implements CoverLetterAiControllerInterface {

    private final CoverLetterAiService coverLetterAiService;

    /**
     * 자소서 내용을 AI로 첨삭/개선한다.
     *
     * @param request      자소서 본문/지원분야/경력/추가요청
     * @param userDetails  인증 사용자 정보
     * @return 개선 결과(피드백/개선본문)
     */
    @PostMapping("/ai-improve")
    @RequireTokens(UsageType.COVERLETTER_REVIEW)
    @Override
    public ResponseEntity<CommonResponse<CoverLetterAiResponse>> improveCoverLetter(
            @Valid @RequestBody CoverLetterAiRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        MDC.put("spanId", "coverletter-ai-controller");

        log.info("AI 첨삭 요청 - 지원분야: {}, 경력: {}, 컨텐츠길이: {}, 커스텀프롬프트: {}",
                request.jobField(),
                request.getTotalExperience(),
                request.content() != null ? request.content().length() : 0,
                request.customPrompt() != null ? "있음" : "없음");

        try {
            CoverLetterAiResponse response = coverLetterAiService.improveCoverLetter(request);

            log.info("AI 첨삭 완료");

            return ResponseEntity.ok(CommonResponse.success("자소서 AI 개선이 완료되었습니다.", response));

        }catch (Exception e) {
            log.error("AI 첨삭 처리 중 예상치 못한 오류", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(CommonResponse.error("UNEXPECTED_ERROR", "처리 중 오류가 발생했습니다.", true));
        }
    }
}
