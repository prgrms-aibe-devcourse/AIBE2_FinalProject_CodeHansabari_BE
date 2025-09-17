package com.cvmento.domain.coverLetter.controller;

import com.cvmento.domain.coverLetter.controller.interfaces.RawCoverLetterFeatureQueryControllerInterface;
import com.cvmento.domain.coverLetter.dto.response.RawCoverLetterFeaturePageResponse;
import com.cvmento.domain.coverLetter.enums.FeaturesCategory;
import com.cvmento.domain.coverLetter.service.RawCoverLetterFeatureQueryService;
import com.cvmento.global.common.dto.CommonResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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

    @GetMapping("/paged")
    @Override
    public ResponseEntity<CommonResponse<RawCoverLetterFeaturePageResponse>> getRawFeaturesPaged(int page, int size, @AuthenticationPrincipal UserDetails userDetails) {
        MDC.put("spanId", "raw-feature-query-controller-all");
        
        String userEmail = userDetails.getUsername();
        log.info("Raw 특징 페이징 조회 요청 - 사용자: {}, 페이지: {}, 크기: {}", userEmail, page, size);
        
        RawCoverLetterFeaturePageResponse response = rawCoverLetterFeatureQueryService.getRawFeaturesPaged(page, size);
        return ResponseEntity.ok(CommonResponse.success(response));
    }

    @GetMapping("/paged/category/{category}")
    @Override
    public ResponseEntity<CommonResponse<RawCoverLetterFeaturePageResponse>> getRawFeaturesByCategoryPaged(FeaturesCategory category, int page, int size, @AuthenticationPrincipal UserDetails userDetails) {
        MDC.put("spanId", "raw-feature-query-controller-category");
        
        String userEmail = userDetails.getUsername();
        log.info("Raw 카테고리별 특징 페이징 조회 요청 - 사용자: {}, 카테고리: {}, 페이지: {}, 크기: {}", userEmail, category, page, size);
        
        RawCoverLetterFeaturePageResponse response = rawCoverLetterFeatureQueryService.getRawFeaturesByCategoryPaged(category, page, size);
        return ResponseEntity.ok(CommonResponse.success(response));
    }
}