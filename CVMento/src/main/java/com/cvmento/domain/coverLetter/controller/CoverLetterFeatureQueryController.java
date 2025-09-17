package com.cvmento.domain.coverLetter.controller;

import com.cvmento.domain.coverLetter.controller.interfaces.CoverLetterFeatureQueryControllerInterface;
import com.cvmento.domain.coverLetter.dto.response.CoverLetterFeatureData;
import com.cvmento.domain.coverLetter.dto.response.CoverLetterFeaturePageResponse;
import com.cvmento.domain.coverLetter.enums.FeaturesCategory;
import com.cvmento.domain.coverLetter.service.CoverLetterFeatureQueryService;
import com.cvmento.global.common.dto.CommonResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 추출된 특징 조회 컨트롤러
 * - 페이징을 통한 특징 조회
 * - 카테고리별 특징 조회
 * - 정렬 옵션 제공
 */
@RestController
@RequestMapping("/api/cover-letter-feature")
@RequiredArgsConstructor
@Slf4j
public class CoverLetterFeatureQueryController implements CoverLetterFeatureQueryControllerInterface {

    private final CoverLetterFeatureQueryService coverLetterFeatureQueryService;

    /**
     * 모든 특징을 페이징으로 조회 (생성일 기준 내림차순)
     */
    @GetMapping("/paged")
    public ResponseEntity<CommonResponse<CoverLetterFeaturePageResponse>> getAllFeaturesWithPagination(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        MDC.put("spanId", "feature-query-controller-all");
        
        try {
            String userEmail = userDetails.getUsername();
            log.info("모든 특징 페이징 조회 요청 - 사용자: {}, 페이지: {}, 크기: {}", 
                    userEmail, page, size);
            
            CoverLetterFeaturePageResponse response = coverLetterFeatureQueryService
                    .getAllFeaturesWithPagination(page, size);
            
            log.info("모든 특징 페이징 조회 완료 - 총 개수: {}, 총 페이지: {}", 
                    response.totalElements(), response.totalPages());
            
            return ResponseEntity.ok(CommonResponse.success(response));
            
        } catch (Exception e) {
            log.error("모든 특징 페이징 조회 중 오류 발생", e);
            return ResponseEntity.ok(CommonResponse.error("QUERY_FAILED", 
                    "특징 조회 중 오류가 발생했습니다: " + e.getMessage()));
        }
    }

    /**
     * 특정 카테고리의 특징들을 페이징으로 조회 (생성일 기준 내림차순)
     */
    @GetMapping("/paged/category/{category}")
    public ResponseEntity<CommonResponse<CoverLetterFeaturePageResponse>> getFeaturesByCategoryWithPagination(
            @PathVariable FeaturesCategory category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        MDC.put("spanId", "feature-query-controller-category");
        
        try {
            String userEmail = userDetails.getUsername();
            log.info("카테고리별 특징 페이징 조회 요청 - 사용자: {}, 카테고리: {}, 페이지: {}, 크기: {}",
                    userEmail, category, page, size);

            CoverLetterFeaturePageResponse response = coverLetterFeatureQueryService
                    .getFeaturesByCategoryWithPagination(category, page, size);

            log.info("카테고리별 특징 페이징 조회 완료 - 카테고리: {}, 총 개수: {}, 총 페이지: {}",
                    category, response.totalElements(), response.totalPages());

            return ResponseEntity.ok(CommonResponse.success(response));

        } catch (Exception e) {
            log.error("카테고리별 특징 페이징 조회 중 오류 발생 - 카테고리: {}", category, e);
            return ResponseEntity.ok(CommonResponse.error("QUERY_FAILED",
                    "카테고리별 특징 조회 중 오류가 발생했습니다: " + e.getMessage()));
        }
    }

    /**
     * 중복횟수 기준 내림차순으로 페이징 조회
     */
    @GetMapping("/paged/duplicate-count")
    public ResponseEntity<CommonResponse<CoverLetterFeaturePageResponse>> getFeaturesByDuplicateCountWithPagination(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal UserDetails userDetails) {

        MDC.put("spanId", "feature-query-controller-duplicate-count");

        try {
            String userEmail = userDetails.getUsername();
            log.info("중복횟수 기준 특징 페이징 조회 요청 - 사용자: {}, 페이지: {}, 크기: {}",
                    userEmail, page, size);

            CoverLetterFeaturePageResponse response = coverLetterFeatureQueryService
                    .getFeaturesByDuplicateCountWithPagination(page, size);

            log.info("중복횟수 기준 특징 페이징 조회 완료 - 총 개수: {}, 총 페이지: {}",
                    response.totalElements(), response.totalPages());

            return ResponseEntity.ok(CommonResponse.success(response));

        } catch (Exception e) {
            log.error("중복횟수 기준 특징 페이징 조회 중 오류 발생", e);
            return ResponseEntity.ok(CommonResponse.error("QUERY_FAILED",
                    "중복횟수 기준 특징 조회 중 오류가 발생했습니다: " + e.getMessage()));
        }
    }

    /**
     * 특정 카테고리에서 중복횟수 기준 내림차순으로 페이징 조회
     */
    @GetMapping("/paged/category/{category}/duplicate-count")
    public ResponseEntity<CommonResponse<CoverLetterFeaturePageResponse>> getFeaturesByCategoryAndDuplicateCountWithPagination(
            @PathVariable FeaturesCategory category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal UserDetails userDetails) {

        MDC.put("spanId", "feature-query-controller-category-duplicate-count");

        try {
            String userEmail = userDetails.getUsername();
            log.info("카테고리별 중복횟수 기준 특징 페이징 조회 요청 - 사용자: {}, 카테고리: {}, 페이지: {}, 크기: {}", 
                    userEmail, category, page, size);
            
            CoverLetterFeaturePageResponse response = coverLetterFeatureQueryService
                    .getFeaturesByCategoryAndDuplicateCountWithPagination(category, page, size);
            
            log.info("카테고리별 중복횟수 기준 특징 페이징 조회 완료 - 카테고리: {}, 총 개수: {}, 총 페이지: {}", 
                    category, response.totalElements(), response.totalPages());
            
            return ResponseEntity.ok(CommonResponse.success(response));
            
        } catch (Exception e) {
            log.error("카테고리별 중복횟수 기준 특징 페이징 조회 중 오류 발생 - 카테고리: {}", category, e);
            return ResponseEntity.ok(CommonResponse.error("QUERY_FAILED", 
                    "카테고리별 중복횟수 기준 특징 조회 중 오류가 발생했습니다: " + e.getMessage()));
        }
    }

    /**
     * 모든 특징 조회 (페이징 없음)
     */
    @GetMapping("/all")
    public ResponseEntity<CommonResponse<List<CoverLetterFeatureData>>> getAllFeatures(
            @AuthenticationPrincipal UserDetails userDetails) {
        
        MDC.put("spanId", "feature-query-controller-all-no-paging");
        
        try {
            String userEmail = userDetails.getUsername();
            log.info("모든 특징 조회 요청 - 사용자: {}", userEmail);
            
            List<CoverLetterFeatureData> response = coverLetterFeatureQueryService.getAllFeatures();
            
            log.info("모든 특징 조회 완료 - 총 개수: {}", response.size());
            
            return ResponseEntity.ok(CommonResponse.success(response));
            
        } catch (Exception e) {
            log.error("모든 특징 조회 중 오류 발생", e);
            return ResponseEntity.ok(CommonResponse.error("QUERY_FAILED", 
                    "특징 조회 중 오류가 발생했습니다: " + e.getMessage()));
        }
    }

    /**
     * 특정 카테고리의 특징들 조회 (페이징 없음)
     */
    @GetMapping("/category/{category}")
    public ResponseEntity<CommonResponse<List<CoverLetterFeatureData>>> getFeaturesByCategory(
            @PathVariable FeaturesCategory category,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        MDC.put("spanId", "feature-query-controller-category-no-paging");
        
        try {
            String userEmail = userDetails.getUsername();
            log.info("카테고리별 특징 조회 요청 - 사용자: {}, 카테고리: {}", 
                    userEmail, category);
            
            List<CoverLetterFeatureData> response = coverLetterFeatureQueryService
                    .getFeaturesByCategory(category);
            
            log.info("카테고리별 특징 조회 완료 - 카테고리: {}, 총 개수: {}", category, response.size());
            
            return ResponseEntity.ok(CommonResponse.success(response));
            
        } catch (Exception e) {
            log.error("카테고리별 특징 조회 중 오류 발생 - 카테고리: {}", category, e);
            return ResponseEntity.ok(CommonResponse.error("QUERY_FAILED", 
                    "카테고리별 특징 조회 중 오류가 발생했습니다: " + e.getMessage()));
        }
    }

    /**
     * 특징 통계 정보 조회
     */
    @GetMapping("/statistics")
    public ResponseEntity<CommonResponse<CoverLetterFeatureQueryService.FeatureStatistics>> getFeatureStatistics(
            @AuthenticationPrincipal UserDetails userDetails) {
        
        MDC.put("spanId", "feature-query-controller-statistics");
        
        try {
            String userEmail = userDetails.getUsername();
            log.info("특징 통계 조회 요청 - 사용자: {}", userEmail);
            
            CoverLetterFeatureQueryService.FeatureStatistics response = 
                    coverLetterFeatureQueryService.getFeatureStatistics();
            
            log.info("특징 통계 조회 완료 - 총 개수: {}, EXPRESSION: {}, STRUCTURE: {}, CONTENT: {}", 
                    response.totalCount(), response.expressionCount(), 
                    response.structureCount(), response.contentCount());
            
            return ResponseEntity.ok(CommonResponse.success(response));
            
        } catch (Exception e) {
            log.error("특징 통계 조회 중 오류 발생", e);
            return ResponseEntity.ok(CommonResponse.error("QUERY_FAILED", 
                    "특징 통계 조회 중 오류가 발생했습니다: " + e.getMessage()));
        }
    }
}
