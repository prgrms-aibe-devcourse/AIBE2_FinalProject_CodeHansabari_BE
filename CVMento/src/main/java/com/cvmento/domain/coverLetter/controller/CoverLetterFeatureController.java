package com.cvmento.domain.coverLetter.controller;

import com.cvmento.domain.auth.service.AuthService;
import com.cvmento.domain.coverLetter.controller.interfaces.CoverLetterFeatureControllerInterface;
import com.cvmento.domain.coverLetter.dto.response.FeatureCandidate;
import com.cvmento.domain.coverLetter.service.CoverLetterFeatureService;
import com.cvmento.domain.member.entity.Member;
import com.cvmento.domain.member.enums.Role;
import com.cvmento.global.common.dto.CommonResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 자소서 특징 추출 API
 */
@RestController
@RequestMapping("/api/cover-letter-feature")
@RequiredArgsConstructor
@Slf4j
public class CoverLetterFeatureController implements CoverLetterFeatureControllerInterface {

    private final CoverLetterFeatureService coverLetterFeatureService;
    private final AuthService authService;

    /**
     * 크롤링된 자소서에서 특징 추출
     */
    @PostMapping("/extract")
    @Override
    public ResponseEntity<CommonResponse<List<FeatureCandidate>>> extractFeatures(@AuthenticationPrincipal UserDetails userDetails) {
        MDC.put("spanId", "feature-extraction-controller");

        if (userDetails == null) {
            throw new AccessDeniedException("인증되지 않은 사용자입니다.");
        }

        Member member = authService.getMemberFromUserDetails(userDetails);
        if (member.getRole() != Role.ADMIN && member.getRole() != Role.ROOT) {
            log.warn("특징 추출 권한 없는 접근 시도 - memberId: {}, role: {}",
                    member.getMemberId(), member.getRole());
            throw new AccessDeniedException("특징 추출을 실행할 권한이 없습니다. 관리자 권한이 필요합니다.");
        }

        log.info("자소서 특징 추출 실행 요청 - 관리자: {}, role: {}",
                member.getMemberId(), member.getRole());

        try {
            List<FeatureCandidate> features = coverLetterFeatureService.extractFeaturesFromCrawledData();
            log.info("특징 추출 실행 성공 - 추출된 특징 개수: {}", features.size());
            return ResponseEntity.ok(CommonResponse.success("특징 추출이 완료되었습니다.", features));
        } catch (Exception e) {
            log.error("특징 추출 컨트롤러 예외 발생", e);
            return ResponseEntity.ok(CommonResponse.error("EXTRACTION_FAILED", "특징 추출 중 오류가 발생했습니다: " + e.getMessage()));
        }
    }

    /**
     * 테스트용: 단일 자소서 특징 추출
     */
    @PostMapping("/test/single")
    @Override
    public ResponseEntity<CommonResponse<List<FeatureCandidate>>> extractFeaturesFromSingle(
            @RequestParam Long essayId,
            @AuthenticationPrincipal UserDetails userDetails) {
        MDC.put("spanId", "feature-test-single-controller");

        if (userDetails == null) {
            throw new AccessDeniedException("인증되지 않은 사용자입니다.");
        }

        Member member = authService.getMemberFromUserDetails(userDetails);
        if (member.getRole() != Role.ADMIN && member.getRole() != Role.ROOT) {
            log.warn("테스트용 특징 추출 권한 없는 접근 시도 - memberId: {}, role: {}",
                    member.getMemberId(), member.getRole());
            throw new AccessDeniedException("테스트용 특징 추출을 실행할 권한이 없습니다. 관리자 권한이 필요합니다.");
        }

        log.info("테스트용 자소서 특징 추출 요청 - 자소서ID: {}, 관리자: {}, role: {}",
                essayId, member.getMemberId(), member.getRole());

        try {
            List<FeatureCandidate> features = coverLetterFeatureService.extractFeaturesFromSingleEssay(essayId);
            log.info("테스트용 특징 추출 실행 성공 - 자소서ID: {}, 추출된 특징 개수: {}", essayId, features.size());
            return ResponseEntity.ok(CommonResponse.success("테스트용 특징 추출이 완료되었습니다.", features));
        } catch (Exception e) {
            log.error("테스트용 특징 추출 컨트롤러 예외 발생 - 자소서ID: {}", essayId, e);
            return ResponseEntity.ok(CommonResponse.error("TEST_EXTRACTION_FAILED", "테스트용 특징 추출 중 오류가 발생했습니다: " + e.getMessage()));
        }
    }
}