package com.cvmento.domain.coverLetter.controller;

import com.cvmento.domain.auth.service.AuthService;
import com.cvmento.domain.coverLetter.controller.interfaces.CoverLetterFeatureQueryControllerInterface;
import com.cvmento.domain.coverLetter.dto.response.CoverLetterFeatureData;
import com.cvmento.domain.coverLetter.dto.response.CoverLetterFeaturePageResponse;
import com.cvmento.domain.coverLetter.enums.FeaturesCategory;
import com.cvmento.domain.coverLetter.service.CoverLetterFeatureQueryService;
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
    private final AuthService authService;

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
            if (userDetails == null) {
                throw new AccessDeniedException("인증되지 않은 사용자입니다.");
            }
            Member member = authService.getMemberFromUserDetails(userDetails);
            if (member.getRole() != Role.ADMIN && member.getRole() != Role.ROOT) {
                log.warn("권한 없는 사용자가 특징 조회 시도 - memberId: {}, role: {}", member.getMemberId(), member.getRole());
                throw new AccessDeniedException("특징을 조회할 권한이 없습니다. 관리자 권한이 필요합니다.");
            }
            
            // 페이지 크기 제한
            if (size > 100) {
                size = 100;
            }
            
            log.info("모든 특징 페이징 조회 요청 - 관리자: {}, role: {}, 페이지: {}, 크기: {}", 
                    member.getMemberId(), member.getRole(), page, size);
            
            CoverLetterFeaturePageResponse response = coverLetterFeatureQueryService
                    .getAllFeaturesWithPagination(page, size);
            
            log.info("모든 특징 페이징 조회 완료 - 총 개수: {}, 총 페이지: {}", 
                    response.totalElements(), response.totalPages());
            
            return ResponseEntity.ok(CommonResponse.success(response));
            
        } catch (Exception e) {
            log.error("모든 특징 페이징 조회 중 오류 발생", e);
            return ResponseEntity.ok(CommonResponse.error("QUERY_FAILED", 
                    "특징 조회 중 오류가 발생했습니다: " + e.getMessage()));
        } finally {
            MDC.remove("spanId");
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
            if (userDetails == null) {
                throw new AccessDeniedException("인증되지 않은 사용자입니다.");
            }
            Member member = authService.getMemberFromUserDetails(userDetails);
            if (member.getRole() != Role.ADMIN && member.getRole() != Role.ROOT) {
                log.warn("권한 없는 사용자가 카테고리별 특징 조회 시도 - memberId: {}, role: {}", member.getMemberId(), member.getRole());
                throw new AccessDeniedException("특징을 조회할 권한이 없습니다. 관리자 권한이 필요합니다.");
            }
            
            // 페이지 크기 제한
            if (size > 100) {
                size = 100;
            }
            
            log.info("카테고리별 특징 페이징 조회 요청 - 관리자: {}, role: {}, 카테고리: {}, 페이지: {}, 크기: {}", 
                    member.getMemberId(), member.getRole(), category, page, size);
            
            CoverLetterFeaturePageResponse response = coverLetterFeatureQueryService
                    .getFeaturesByCategoryWithPagination(category, page, size);
            
            log.info("카테고리별 특징 페이징 조회 완료 - 카테고리: {}, 총 개수: {}, 총 페이지: {}", 
                    category, response.totalElements(), response.totalPages());
            
            return ResponseEntity.ok(CommonResponse.success(response));
            
        } catch (Exception e) {
            log.error("카테고리별 특징 페이징 조회 중 오류 발생 - 카테고리: {}", category, e);
            return ResponseEntity.ok(CommonResponse.error("QUERY_FAILED", 
                    "카테고리별 특징 조회 중 오류가 발생했습니다: " + e.getMessage()));
        } finally {
            MDC.remove("spanId");
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
            if (userDetails == null) {
                throw new AccessDeniedException("인증되지 않은 사용자입니다.");
            }
            Member member = authService.getMemberFromUserDetails(userDetails);
            if (member.getRole() != Role.ADMIN && member.getRole() != Role.ROOT) {
                log.warn("권한 없는 사용자가 중복횟수 기준 특징 조회 시도 - memberId: {}, role: {}", member.getMemberId(), member.getRole());
                throw new AccessDeniedException("특징을 조회할 권한이 없습니다. 관리자 권한이 필요합니다.");
            }
            
            // 페이지 크기 제한
            if (size > 100) {
                size = 100;
            }
            
            log.info("중복횟수 기준 특징 페이징 조회 요청 - 관리자: {}, role: {}, 페이지: {}, 크기: {}", 
                    member.getMemberId(), member.getRole(), page, size);
            
            CoverLetterFeaturePageResponse response = coverLetterFeatureQueryService
                    .getFeaturesByDuplicateCountWithPagination(page, size);
            
            log.info("중복횟수 기준 특징 페이징 조회 완료 - 총 개수: {}, 총 페이지: {}", 
                    response.totalElements(), response.totalPages());
            
            return ResponseEntity.ok(CommonResponse.success(response));
            
        } catch (Exception e) {
            log.error("중복횟수 기준 특징 페이징 조회 중 오류 발생", e);
            return ResponseEntity.ok(CommonResponse.error("QUERY_FAILED", 
                    "중복횟수 기준 특징 조회 중 오류가 발생했습니다: " + e.getMessage()));
        } finally {
            MDC.remove("spanId");
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
            if (userDetails == null) {
                throw new AccessDeniedException("인증되지 않은 사용자입니다.");
            }
            Member member = authService.getMemberFromUserDetails(userDetails);
            if (member.getRole() != Role.ADMIN && member.getRole() != Role.ROOT) {
                log.warn("권한 없는 사용자가 카테고리별 중복횟수 기준 특징 조회 시도 - memberId: {}, role: {}", member.getMemberId(), member.getRole());
                throw new AccessDeniedException("특징을 조회할 권한이 없습니다. 관리자 권한이 필요합니다.");
            }
            
            // 페이지 크기 제한
            if (size > 100) {
                size = 100;
            }
            
            log.info("카테고리별 중복횟수 기준 특징 페이징 조회 요청 - 관리자: {}, role: {}, 카테고리: {}, 페이지: {}, 크기: {}", 
                    member.getMemberId(), member.getRole(), category, page, size);
            
            CoverLetterFeaturePageResponse response = coverLetterFeatureQueryService
                    .getFeaturesByCategoryAndDuplicateCountWithPagination(category, page, size);
            
            log.info("카테고리별 중복횟수 기준 특징 페이징 조회 완료 - 카테고리: {}, 총 개수: {}, 총 페이지: {}", 
                    category, response.totalElements(), response.totalPages());
            
            return ResponseEntity.ok(CommonResponse.success(response));
            
        } catch (Exception e) {
            log.error("카테고리별 중복횟수 기준 특징 페이징 조회 중 오류 발생 - 카테고리: {}", category, e);
            return ResponseEntity.ok(CommonResponse.error("QUERY_FAILED", 
                    "카테고리별 중복횟수 기준 특징 조회 중 오류가 발생했습니다: " + e.getMessage()));
        } finally {
            MDC.remove("spanId");
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
            if (userDetails == null) {
                throw new AccessDeniedException("인증되지 않은 사용자입니다.");
            }
            Member member = authService.getMemberFromUserDetails(userDetails);
            if (member.getRole() != Role.ADMIN && member.getRole() != Role.ROOT) {
                log.warn("권한 없는 사용자가 모든 특징 조회 시도 - memberId: {}, role: {}", member.getMemberId(), member.getRole());
                throw new AccessDeniedException("특징을 조회할 권한이 없습니다. 관리자 권한이 필요합니다.");
            }
            
            log.info("모든 특징 조회 요청 - 관리자: {}, role: {}", member.getMemberId(), member.getRole());
            
            List<CoverLetterFeatureData> response = coverLetterFeatureQueryService.getAllFeatures();
            
            log.info("모든 특징 조회 완료 - 총 개수: {}", response.size());
            
            return ResponseEntity.ok(CommonResponse.success(response));
            
        } catch (Exception e) {
            log.error("모든 특징 조회 중 오류 발생", e);
            return ResponseEntity.ok(CommonResponse.error("QUERY_FAILED", 
                    "특징 조회 중 오류가 발생했습니다: " + e.getMessage()));
        } finally {
            MDC.remove("spanId");
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
            if (userDetails == null) {
                throw new AccessDeniedException("인증되지 않은 사용자입니다.");
            }
            Member member = authService.getMemberFromUserDetails(userDetails);
            if (member.getRole() != Role.ADMIN && member.getRole() != Role.ROOT) {
                log.warn("권한 없는 사용자가 카테고리별 특징 조회 시도 - memberId: {}, role: {}", member.getMemberId(), member.getRole());
                throw new AccessDeniedException("특징을 조회할 권한이 없습니다. 관리자 권한이 필요합니다.");
            }
            
            log.info("카테고리별 특징 조회 요청 - 관리자: {}, role: {}, 카테고리: {}", 
                    member.getMemberId(), member.getRole(), category);
            
            List<CoverLetterFeatureData> response = coverLetterFeatureQueryService
                    .getFeaturesByCategory(category);
            
            log.info("카테고리별 특징 조회 완료 - 카테고리: {}, 총 개수: {}", category, response.size());
            
            return ResponseEntity.ok(CommonResponse.success(response));
            
        } catch (Exception e) {
            log.error("카테고리별 특징 조회 중 오류 발생 - 카테고리: {}", category, e);
            return ResponseEntity.ok(CommonResponse.error("QUERY_FAILED", 
                    "카테고리별 특징 조회 중 오류가 발생했습니다: " + e.getMessage()));
        } finally {
            MDC.remove("spanId");
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
            if (userDetails == null) {
                throw new AccessDeniedException("인증되지 않은 사용자입니다.");
            }
            Member member = authService.getMemberFromUserDetails(userDetails);
            if (member.getRole() != Role.ADMIN && member.getRole() != Role.ROOT) {
                log.warn("권한 없는 사용자가 특징 통계 조회 시도 - memberId: {}, role: {}", member.getMemberId(), member.getRole());
                throw new AccessDeniedException("특징을 조회할 권한이 없습니다. 관리자 권한이 필요합니다.");
            }
            
            log.info("특징 통계 조회 요청 - 관리자: {}, role: {}", member.getMemberId(), member.getRole());
            
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
        } finally {
            MDC.remove("spanId");
        }
    }
}
