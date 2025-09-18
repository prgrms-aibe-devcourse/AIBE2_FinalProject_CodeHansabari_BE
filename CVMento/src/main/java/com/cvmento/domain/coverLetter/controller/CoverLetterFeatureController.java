package com.cvmento.domain.coverLetter.controller;

import com.cvmento.domain.coverLetter.controller.interfaces.CoverLetterFeatureControllerInterface;
import com.cvmento.domain.coverLetter.dto.response.CoverLetterFeatureData;
import com.cvmento.domain.coverLetter.dto.response.RawCoverLetterFeatureData;
import com.cvmento.domain.coverLetter.entity.CoverLetterFeature;
import com.cvmento.domain.coverLetter.entity.RawCoverLetterFeature;
import com.cvmento.domain.coverLetter.enums.FeaturesCategory;
import com.cvmento.domain.coverLetter.service.CoverLetterFeatureQueryService;
import com.cvmento.domain.coverLetter.service.CoverLetterFeatureService;
import com.cvmento.domain.coverLetter.service.FarthestFirstClusteringService;
import com.cvmento.domain.coverLetter.service.RawCoverLetterFeatureQueryService;
import com.cvmento.global.common.dto.CommonResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 자소서 특징 추출 및 조회 API
 */
@RestController
@RequestMapping("/api/cover-letter-features")
@RequiredArgsConstructor
@Slf4j
public class CoverLetterFeatureController implements CoverLetterFeatureControllerInterface {

    private final CoverLetterFeatureService coverLetterFeatureService;
    // private final FarthestFirstClusteringService farthestFirstClusteringService;  // 임시로 비활성화 (다른 서버로 분리 예정)
    private final CoverLetterFeatureQueryService coverLetterFeatureQueryService;
    private final RawCoverLetterFeatureQueryService rawCoverLetterFeatureQueryService;

    /**
     * 크롤링된 자소서에서 특징 추출
     */
    @PostMapping("/extract")
    @Override
    public ResponseEntity<CommonResponse<List<RawCoverLetterFeature>>> extractFeatures(@AuthenticationPrincipal UserDetails userDetails) {
        MDC.put("spanId", "feature-extraction-controller");

        String userEmail = userDetails.getUsername();
        log.info("자소서 특징 추출 실행 요청 - 사용자: {}", userEmail);

        List<RawCoverLetterFeature> rawFeatures = coverLetterFeatureService.extractFeaturesFromCrawledData();
        log.info("특징 추출 실행 성공 - 추출된 특징 개수: {}", rawFeatures.size());
        return ResponseEntity.ok(CommonResponse.success("특징 추출이 완료되었습니다. raw_features 테이블에 저장되었습니다.", rawFeatures));
    }

    /**
     * 임베딩 기반 특징 중복제거 수행
     */
    // @PostMapping("/deduplicate")  // 임시로 비활성화 (다른 서버로 분리 예정)
//    public ResponseEntity<CommonResponse<List<CoverLetterFeature>>> deduplicateFeatures(
//            @AuthenticationPrincipal UserDetails userDetails) {
//        MDC.put("spanId", "feature-deduplication-controller");
//
//        String userEmail = userDetails.getUsername();
//        log.info("임베딩 기반 특징 중복제거 실행 요청 - 사용자: {}", userEmail);
//
//        List<CoverLetterFeature> finalFeatures = farthestFirstClusteringService.deduplicateFeaturesWithFarthestFirst();
//        log.info("특징 중복제거 실행 성공 - 최종 특징 개수: {}", finalFeatures.size());
//        return ResponseEntity.ok(CommonResponse.success("Farthest-First 클러스터링 기반 특징 중복제거가 완료되었습니다. cover_letter_features 테이블에 저장되었습니다.", finalFeatures));
//    }


    // @PostMapping("/process")  // 임시로 비활성화 (다른 서버로 분리 예정)
//    @Override
//    public ResponseEntity<CommonResponse<Object>> extractFeaturesWithRealtimeAPI(UserDetails userDetails) {
//        log.info("전체 특징 처리 시작 - 사용자: {}", userDetails.getUsername());
//
//        // 1단계: 특징 추출
//        log.info("1단계: 특징 추출 시작");
//        List<RawCoverLetterFeature> rawFeatures = coverLetterFeatureService.extractFeaturesFromCrawledData();
//        log.info("1단계 완료: {}개 특징 추출", rawFeatures.size());
//
//        // 2단계: 중복제거
//        log.info("2단계: Farthest-First 클러스터링 기반 중복제거 시작");
//        List<CoverLetterFeature> finalFeatures = farthestFirstClusteringService.deduplicateFeaturesWithFarthestFirst();
//        log.info("2단계 완료: {}개 최종 특징 선정", finalFeatures.size());
//
//        // 결과 요약 생성
//        Map<String, Object> result = new HashMap<>();
//        result.put("rawFeaturesCount", rawFeatures.size());
//        result.put("finalFeaturesCount", finalFeatures.size());
//        result.put("deduplicationRatio", String.format("%.1f%%", (1.0 - (double) finalFeatures.size() / rawFeatures.size()) * 100));
//        result.put("batchSize", 2);
//        result.put("totalBatches", (int) Math.ceil(rawFeatures.size() / 6.0)); // 2개 자소서 * 3개 특징 = 6개
//        result.put("status", "COMPLETE");
//        result.put("message", "전체 특징 처리가 완료되었습니다.");
//
//        log.info("전체 특징 처리 완료 - 원본: {}개, 최종: {}개", rawFeatures.size(), finalFeatures.size());
//
//        return ResponseEntity.ok(CommonResponse.success("전체 특징 처리가 완료되었습니다.", result));
//    }

    /**
     * 모든 특징을 페이징으로 조회 (생성일 기준 내림차순)
     */
    @GetMapping("/")
    public ResponseEntity<CommonResponse<Page<CoverLetterFeatureData>>> getAllFeaturesWithPagination(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            @AuthenticationPrincipal UserDetails userDetails) {

        MDC.put("spanId", "feature-query-controller-all");

        String userEmail = userDetails.getUsername();
        log.info("모든 특징 페이징 조회 요청 - 사용자: {}, 페이지: {}, 크기: {}",
                userEmail, pageable.getPageNumber(), pageable.getPageSize());

        Page<CoverLetterFeatureData> response = coverLetterFeatureQueryService
                .getAllFeaturesWithPagination(pageable);

        log.info("모든 특징 페이징 조회 완료 - 총 개수: {}, 총 페이지: {}",
                response.getTotalElements(), response.getTotalPages());

        return ResponseEntity.ok(CommonResponse.success(response));
    }

    /**
     * 특정 카테고리의 특징들을 페이징으로 조회 (생성일 기준 내림차순)
     */
    @GetMapping("/category/{category}")
    public ResponseEntity<CommonResponse<Page<CoverLetterFeatureData>>> getFeaturesByCategoryWithPagination(
            @PathVariable FeaturesCategory category,
            @PageableDefault(size = 20, sort = "duplicateCount", direction = Sort.Direction.DESC) Pageable pageable,
            @AuthenticationPrincipal UserDetails userDetails) {

        MDC.put("spanId", "feature-query-controller-category");

        String userEmail = userDetails.getUsername();
        log.info("카테고리별 특징 페이징 조회 요청 - 사용자: {}, 카테고리: {}, 페이지: {}, 크기: {}",
                userEmail, category, pageable.getPageNumber(), pageable.getPageSize());

        Page<CoverLetterFeatureData> response = coverLetterFeatureQueryService
                .getFeaturesByCategoryAndDuplicateCountWithPagination(category, pageable);

        log.info("카테고리별 특징 페이징 조회 완료 - 카테고리: {}, 총 개수: {}, 총 페이지: {}",
                category, response.getTotalElements(), response.getTotalPages());

        return ResponseEntity.ok(CommonResponse.success(response));
    }

    /**
     * 특징 통계 정보 조회
     */
    @GetMapping("/statistics")
    public ResponseEntity<CommonResponse<CoverLetterFeatureQueryService.FeatureStatistics>> getFeatureStatistics(
            @AuthenticationPrincipal UserDetails userDetails) {

        MDC.put("spanId", "feature-query-controller-statistics");

        String userEmail = userDetails.getUsername();
        log.info("특징 통계 조회 요청 - 사용자: {}", userEmail);

        CoverLetterFeatureQueryService.FeatureStatistics response =
                coverLetterFeatureQueryService.getFeatureStatistics();

        log.info("특징 통계 조회 완료 - 총 개수: {}, EXPRESSION: {}, STRUCTURE: {}, CONTENT: {}",
                response.totalCount(), response.expressionCount(),
                response.structureCount(), response.contentCount());

        return ResponseEntity.ok(CommonResponse.success(response));
    }

    /**
     * Raw 특징 페이징 조회
     */
    @GetMapping("/raw")
    @Override
    public ResponseEntity<CommonResponse<Page<RawCoverLetterFeatureData>>> getRawFeaturesPaged(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            @AuthenticationPrincipal UserDetails userDetails) {
        MDC.put("spanId", "raw-feature-query-controller-all");

        String userEmail = userDetails.getUsername();
        log.info("Raw 특징 페이징 조회 요청 - 사용자: {}, 페이지: {}, 크기: {}", userEmail, pageable.getPageNumber(), pageable.getPageSize());

        Page<RawCoverLetterFeatureData> response = rawCoverLetterFeatureQueryService.getRawFeaturesPaged(pageable);
        return ResponseEntity.ok(CommonResponse.success(response));
    }

    /**
     * 카테고리별 Raw 특징 페이징 조회
     */
    @GetMapping("/raw/category/{category}")
    @Override
    public ResponseEntity<CommonResponse<Page<RawCoverLetterFeatureData>>> getRawFeaturesByCategoryPaged(
            @PathVariable FeaturesCategory category,
            @PageableDefault(size = 20, sort = "duplicateCount", direction = Sort.Direction.DESC) Pageable pageable,
            @AuthenticationPrincipal UserDetails userDetails) {
        MDC.put("spanId", "raw-feature-query-controller-category");

        String userEmail = userDetails.getUsername();
        log.info("Raw 카테고리별 특징 페이징 조회 요청 - 사용자: {}, 카테고리: {}, 페이지: {}, 크기: {}", userEmail, category, pageable.getPageNumber(), pageable.getPageSize());

        Page<RawCoverLetterFeatureData> response = rawCoverLetterFeatureQueryService.getRawFeaturesByCategoryPaged(category, pageable);
        return ResponseEntity.ok(CommonResponse.success(response));
    }
}