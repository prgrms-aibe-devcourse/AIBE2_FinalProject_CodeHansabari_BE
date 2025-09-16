package com.cvmento.domain.coverLetter.controller;

import com.cvmento.domain.auth.service.AuthService;
import com.cvmento.domain.coverLetter.controller.interfaces.RawCoverLetterFeatureQueryControllerInterface;
import com.cvmento.domain.coverLetter.dto.response.RawCoverLetterFeaturePageResponse;
import com.cvmento.domain.coverLetter.enums.FeaturesCategory;
import com.cvmento.domain.coverLetter.service.RawCoverLetterFeatureQueryService;
import com.cvmento.domain.member.entity.Member;
import com.cvmento.domain.member.enums.Role;
import com.cvmento.global.common.dto.CommonResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/raw-cover-letter-feature")
@RequiredArgsConstructor
@Slf4j
public class RawCoverLetterFeatureQueryController implements RawCoverLetterFeatureQueryControllerInterface {

    private final RawCoverLetterFeatureQueryService rawCoverLetterFeatureQueryService;
    private final AuthService authService;

    @GetMapping("/paged")
    @Override
    public ResponseEntity<CommonResponse<RawCoverLetterFeaturePageResponse>> getRawFeaturesPaged(int page, int size, UserDetails userDetails) {
        MDC.put("spanId", "raw-feature-query-controller-all");
        try {
            if (userDetails == null) {
                throw new AccessDeniedException("인증되지 않은 사용자입니다.");
            }
            Member member = authService.getMemberFromUserDetails(userDetails);
            if (member.getRole() != Role.ADMIN && member.getRole() != Role.ROOT) {
                log.warn("권한 없는 사용자가 Raw 특징 조회 시도 - memberId: {}, role: {}", member.getMemberId(), member.getRole());
                throw new AccessDeniedException("Raw 특징을 조회할 권한이 없습니다. 관리자 권한이 필요합니다.");
            }
            RawCoverLetterFeaturePageResponse response = rawCoverLetterFeatureQueryService.getRawFeaturesPaged(page, size);
            return ResponseEntity.ok(CommonResponse.success(response));
        } finally {
            MDC.remove("spanId");
        }
    }

    @GetMapping("/paged/category/{category}")
    @Override
    public ResponseEntity<CommonResponse<RawCoverLetterFeaturePageResponse>> getRawFeaturesByCategoryPaged(FeaturesCategory category, int page, int size, UserDetails userDetails) {
        MDC.put("spanId", "raw-feature-query-controller-category");
        try {
            if (userDetails == null) {
                throw new AccessDeniedException("인증되지 않은 사용자입니다.");
            }
            Member member = authService.getMemberFromUserDetails(userDetails);
            if (member.getRole() != Role.ADMIN && member.getRole() != Role.ROOT) {
                log.warn("권한 없는 사용자가 Raw 카테고리별 특징 조회 시도 - memberId: {}, role: {}", member.getMemberId(), member.getRole());
                throw new AccessDeniedException("Raw 특징을 조회할 권한이 없습니다. 관리자 권한이 필요합니다.");
            }
            RawCoverLetterFeaturePageResponse response = rawCoverLetterFeatureQueryService.getRawFeaturesByCategoryPaged(category, page, size);
            return ResponseEntity.ok(CommonResponse.success(response));
        } finally {
            MDC.remove("spanId");
        }
    }
}


