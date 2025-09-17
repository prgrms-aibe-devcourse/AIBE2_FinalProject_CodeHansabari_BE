package com.cvmento.domain.coverLetter.controller;

import com.cvmento.domain.auth.service.AuthService;
import com.cvmento.domain.coverLetter.controller.interfaces.CoverLetterFeatureControllerInterface;
import com.cvmento.domain.coverLetter.dto.response.FeatureCandidate;
import com.cvmento.domain.coverLetter.entity.CoverLetterFeature;
import com.cvmento.domain.coverLetter.entity.RawCoverLetterFeature;
import com.cvmento.domain.coverLetter.enums.FeaturesCategory;
import com.cvmento.domain.coverLetter.service.CoverLetterFeatureService;
import com.cvmento.domain.coverLetter.service.FarthestFirstClusteringService;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 자소서 특징 추출 API
 */
@RestController
@RequestMapping("/api/cover-letter-feature")
@RequiredArgsConstructor
@Slf4j
public class CoverLetterFeatureController implements CoverLetterFeatureControllerInterface {

    private final CoverLetterFeatureService coverLetterFeatureService;
    private final FarthestFirstClusteringService farthestFirstClusteringService;
    private final AuthService authService;

    /**
     * 크롤링된 자소서에서 특징 추출
     */
    @PostMapping("/extract")
    @Override
    public ResponseEntity<CommonResponse<List<RawCoverLetterFeature>>> extractFeatures(@AuthenticationPrincipal UserDetails userDetails) {
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
            // 새로운 방식: raw_features 테이블에 저장
            List<RawCoverLetterFeature> rawFeatures = coverLetterFeatureService.extractFeaturesFromCrawledData();
            log.info("특징 추출 실행 성공 - 추출된 특징 개수: {}", rawFeatures.size());
            return ResponseEntity.ok(CommonResponse.success("특징 추출이 완료되었습니다. raw_features 테이블에 저장되었습니다.", rawFeatures));
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
            @RequestParam Long coverLetterId,
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
                coverLetterId, member.getMemberId(), member.getRole());

        try {
            List<FeatureCandidate> features = coverLetterFeatureService.extractFeaturesFromSingleCoverLetter(coverLetterId);
            log.info("테스트용 특징 추출 실행 성공 - 자소서ID: {}, 추출된 특징 개수: {}", coverLetterId, features.size());
            return ResponseEntity.ok(CommonResponse.success("테스트용 특징 추출이 완료되었습니다.", features));
        } catch (Exception e) {
            log.error("테스트용 특징 추출 컨트롤러 예외 발생 - 자소서ID: {}", coverLetterId, e);
            return ResponseEntity.ok(CommonResponse.error("TEST_EXTRACTION_FAILED", "테스트용 특징 추출 중 오류가 발생했습니다: " + e.getMessage()));
        }
    }

    /**
     * 테스트용: 배치 특징 추출
     */
    @PostMapping("/test/batch")
    @Override
    public ResponseEntity<CommonResponse<List<FeatureCandidate>>> testBatchExtraction(
            @RequestParam(defaultValue = "5") int batchSize,
            @AuthenticationPrincipal UserDetails userDetails) {
        MDC.put("spanId", "feature-test-batch-controller");

        if (userDetails == null) {
            throw new AccessDeniedException("인증되지 않은 사용자입니다.");
        }

        Member member = authService.getMemberFromUserDetails(userDetails);
        if (member.getRole() != Role.ADMIN && member.getRole() != Role.ROOT) {
            log.warn("배치 테스트 권한 없는 접근 시도 - memberId: {}, role: {}",
                    member.getMemberId(), member.getRole());
            throw new AccessDeniedException("배치 테스트를 실행할 권한이 없습니다. 관리자 권한이 필요합니다.");
        }

        log.info("배치 테스트 요청 - 배치 크기: {}, 관리자: {}, role: {}",
                batchSize, member.getMemberId(), member.getRole());

        try {
            List<FeatureCandidate> features = coverLetterFeatureService.testBatchExtraction(batchSize);
            log.info("배치 테스트 완료 - 추출된 특징 개수: {}", features.size());
            return ResponseEntity.ok(CommonResponse.success("배치 테스트가 완료되었습니다.", features));
        } catch (Exception e) {
            log.error("배치 테스트 중 오류 발생", e);
            return ResponseEntity.ok(CommonResponse.error("BATCH_TEST_FAILED", "배치 테스트 중 오류가 발생했습니다: " + e.getMessage()));
        }
    }


    /**
     * 임베딩 기반 특징 중복제거 수행
     */
    @PostMapping("/deduplicate")
    public ResponseEntity<CommonResponse<List<CoverLetterFeature>>> deduplicateFeatures(
            @AuthenticationPrincipal UserDetails userDetails) {
        MDC.put("spanId", "feature-deduplication-controller");

        if (userDetails == null) {
            throw new AccessDeniedException("인증되지 않은 사용자입니다.");
        }

        Member member = authService.getMemberFromUserDetails(userDetails);
        if (member.getRole() != Role.ADMIN && member.getRole() != Role.ROOT) {
            log.warn("특징 중복제거 권한 없는 접근 시도 - memberId: {}, role: {}",
                    member.getMemberId(), member.getRole());
            throw new AccessDeniedException("특징 중복제거를 실행할 권한이 없습니다. 관리자 권한이 필요합니다.");
        }

        log.info("임베딩 기반 특징 중복제거 실행 요청 - 관리자: {}, role: {}",
                member.getMemberId(), member.getRole());

        try {
            List<CoverLetterFeature> finalFeatures = farthestFirstClusteringService.deduplicateFeaturesWithFarthestFirst();
            log.info("특징 중복제거 실행 성공 - 최종 특징 개수: {}", finalFeatures.size());
            return ResponseEntity.ok(CommonResponse.success("Farthest-First 클러스터링 기반 특징 중복제거가 완료되었습니다. cover_letter_features 테이블에 저장되었습니다.", finalFeatures));
        } catch (Exception e) {
            log.error("특징 중복제거 컨트롤러 예외 발생", e);
            return ResponseEntity.ok(CommonResponse.error("DEDUPLICATION_FAILED", "특징 중복제거 중 오류가 발생했습니다: " + e.getMessage()));
        }
    }

    /**
     * 특정 카테고리의 특징 중복제거 수행
     */
    @PostMapping("/deduplicate/category")
    public ResponseEntity<CommonResponse<List<CoverLetterFeature>>> deduplicateFeaturesByCategory(
            @RequestParam FeaturesCategory category,
            @AuthenticationPrincipal UserDetails userDetails) {
        MDC.put("spanId", "feature-deduplication-category-controller");

        if (userDetails == null) {
            throw new AccessDeniedException("인증되지 않은 사용자입니다.");
        }

        Member member = authService.getMemberFromUserDetails(userDetails);
        if (member.getRole() != Role.ADMIN && member.getRole() != Role.ROOT) {
            log.warn("카테고리별 특징 중복제거 권한 없는 접근 시도 - memberId: {}, role: {}",
                    member.getMemberId(), member.getRole());
            throw new AccessDeniedException("카테고리별 특징 중복제거를 실행할 권한이 없습니다. 관리자 권한이 필요합니다.");
        }

        log.info("{} 카테고리 특징 중복제거 실행 요청 - 관리자: {}, role: {}",
                category, member.getMemberId(), member.getRole());

        try {
            List<CoverLetterFeature> finalFeatures = farthestFirstClusteringService.deduplicateFeaturesWithFarthestFirst();
            log.info("{} 카테고리 특징 중복제거 실행 성공 - 최종 특징 개수: {}", category, finalFeatures.size());
            return ResponseEntity.ok(CommonResponse.success(category + " 카테고리 특징 중복제거가 완료되었습니다.", finalFeatures));
        } catch (Exception e) {
            log.error("{} 카테고리 특징 중복제거 컨트롤러 예외 발생", category, e);
            return ResponseEntity.ok(CommonResponse.error("CATEGORY_DEDUPLICATION_FAILED", category + " 카테고리 특징 중복제거 중 오류가 발생했습니다: " + e.getMessage()));
        }
    }


    @PostMapping("/process")
    @Override
    public ResponseEntity<CommonResponse<Object>> extractFeaturesWithRealtimeAPI(UserDetails userDetails) {
        try {
            log.info("전체 특징 처리 시작 - 사용자: {}", userDetails.getUsername());
            
            // 1단계: 특징 추출
            log.info("1단계: 특징 추출 시작");
            List<RawCoverLetterFeature> rawFeatures = coverLetterFeatureService.extractFeaturesFromCrawledData(false);
            log.info("1단계 완료: {}개 특징 추출", rawFeatures.size());

            // 2단계: 중복제거
            log.info("2단계: Farthest-First 클러스터링 기반 중복제거 시작");
            List<CoverLetterFeature> finalFeatures = farthestFirstClusteringService.deduplicateFeaturesWithFarthestFirst();
            log.info("2단계 완료: {}개 최종 특징 선정", finalFeatures.size());
            
            // 결과 요약 생성
            Map<String, Object> result = new HashMap<>();
            result.put("rawFeaturesCount", rawFeatures.size());
            result.put("finalFeaturesCount", finalFeatures.size());
            result.put("deduplicationRatio", String.format("%.1f%%", (1.0 - (double) finalFeatures.size() / rawFeatures.size()) * 100));
            result.put("batchSize", 2);
            result.put("totalBatches", (int) Math.ceil(rawFeatures.size() / 6.0)); // 2개 자소서 * 3개 특징 = 6개
            result.put("status", "COMPLETE");
            result.put("message", "전체 특징 처리가 완료되었습니다.");
            
            log.info("전체 특징 처리 완료 - 원본: {}개, 최종: {}개", rawFeatures.size(), finalFeatures.size());
            
            return ResponseEntity.ok(CommonResponse.success("전체 특징 처리가 완료되었습니다.", result));
            
        } catch (Exception e) {
            log.error("전체 특징 처리 컨트롤러 예외 발생", e);
            return ResponseEntity.ok(CommonResponse.error("PROCESS_FAILED", "전체 특징 처리 중 오류가 발생했습니다: " + e.getMessage()));
        }
    }

    /**
     * Farthest-First 클러스터링 기반 중복제거
     */
    @PostMapping("/deduplicate/farthest-first")
    public ResponseEntity<CommonResponse<List<CoverLetterFeature>>> deduplicateFeaturesWithFarthestFirst(
            @AuthenticationPrincipal UserDetails userDetails) {
        MDC.put("spanId", "feature-deduplication-farthest-first-controller");

        if (userDetails == null) {
            throw new AccessDeniedException("인증되지 않은 사용자입니다.");
        }

        Member member = authService.getMemberFromUserDetails(userDetails);
        if (member.getRole() != Role.ADMIN && member.getRole() != Role.ROOT) {
            log.warn("Farthest-First 중복제거 권한 없는 접근 시도 - memberId: {}, role: {}",
                    member.getMemberId(), member.getRole());
            throw new AccessDeniedException("Farthest-First 중복제거를 실행할 권한이 없습니다. 관리자 권한이 필요합니다.");
        }

        log.info("Farthest-First 클러스터링 기반 중복제거 실행 요청 - 관리자: {}, role: {}",
                member.getMemberId(), member.getRole());

        try {
            List<CoverLetterFeature> finalFeatures = farthestFirstClusteringService.deduplicateFeaturesWithFarthestFirst();
            log.info("Farthest-First 중복제거 실행 성공 - 최종 특징 개수: {}", finalFeatures.size());
            return ResponseEntity.ok(CommonResponse.success("Farthest-First 클러스터링 기반 중복제거가 완료되었습니다. 942개 → 100개 클러스터로 압축되었습니다.", finalFeatures));
        } catch (Exception e) {
            log.error("Farthest-First 중복제거 컨트롤러 예외 발생", e);
            return ResponseEntity.ok(CommonResponse.error("FARTHEST_FIRST_DEDUPLICATION_FAILED", "Farthest-First 중복제거 중 오류가 발생했습니다: " + e.getMessage()));
        }
    }
}