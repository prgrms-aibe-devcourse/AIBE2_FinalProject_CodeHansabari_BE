package com.cvmento.domain.coverLetter.controller;

import com.cvmento.domain.coverLetter.entity.CoverLetterFeature;
import com.cvmento.domain.coverLetter.enums.FeaturesCategory;
import com.cvmento.domain.coverLetter.repository.CoverLetterFeatureRepository;
import com.cvmento.global.common.dto.CommonResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Sub 백엔드에서 중복 제거 결과를 받아서 메인 DB에 저장하는 Internal API
 */
@RestController
@RequestMapping("/api/internal/features")
@RequiredArgsConstructor
@Slf4j
public class InternalFeatureController {

    private final CoverLetterFeatureRepository coverLetterFeatureRepository;

    /**
     * Sub 백엔드에서 중복 제거 결과를 받아서 메인 DB에 저장
     * 
     * @param features Sub 백엔드에서 전송한 중복 제거된 특징 목록
     * @return 저장 결과
     */
    @PostMapping("/dedup/save")
    @Transactional
    public ResponseEntity<CommonResponse<Map<String, Object>>> saveDedupFeatures(
            @RequestBody List<Map<String, Object>> features) {
        
        log.info("Sub 백엔드로부터 중복 제거 결과 수신 - 특징 개수: {}", features.size());

        try {
            // 기존 데이터 삭제 (중복 제거 결과로 완전 교체)
            coverLetterFeatureRepository.deleteAll();
            log.info("기존 cover_letter_features 데이터 삭제 완료");

            // Sub 백엔드에서 받은 데이터를 CoverLetterFeature 엔티티로 변환
            List<CoverLetterFeature> coverLetterFeatures = new ArrayList<>();
            
            for (Map<String, Object> featureData : features) {
                try {
                    FeaturesCategory category = FeaturesCategory.valueOf((String) featureData.get("featuresCategory"));
                    String description = (String) featureData.get("description");
                    Integer duplicateCount = (Integer) featureData.get("duplicateCount");
                    
                    // representativeCoverLetterId는 Sub 백엔드에서 안보낼 수도 있으므로 기본값 설정
                    Long representativeCoverLetterId = featureData.get("representativeCoverLetterId") != null 
                        ? ((Number) featureData.get("representativeCoverLetterId")).longValue()
                        : 1L;

                    CoverLetterFeature feature = new CoverLetterFeature(
                        category, 
                        description, 
                        duplicateCount != null ? duplicateCount : 1,
                        representativeCoverLetterId
                    );
                    
                    coverLetterFeatures.add(feature);
                    
                } catch (Exception e) {
                    log.warn("특징 데이터 변환 실패, 건너뜀: {}, 오류: {}", featureData, e.getMessage());
                }
            }

            // 메인 DB에 저장
            List<CoverLetterFeature> savedFeatures = coverLetterFeatureRepository.saveAll(coverLetterFeatures);
            
            log.info("중복 제거 결과 메인 DB 저장 완료 - 저장된 특징 개수: {}", savedFeatures.size());

            // 카테고리별 통계 계산
            long expressionCount = savedFeatures.stream()
                .filter(f -> f.getFeaturesCategory() == FeaturesCategory.EXPRESSION)
                .count();
            long structureCount = savedFeatures.stream()
                .filter(f -> f.getFeaturesCategory() == FeaturesCategory.STRUCTURE)
                .count();
            long contentCount = savedFeatures.stream()
                .filter(f -> f.getFeaturesCategory() == FeaturesCategory.CONTENT)
                .count();

            Map<String, Object> result = Map.of(
                "totalSaved", savedFeatures.size(),
                "expressionCount", expressionCount,
                "structureCount", structureCount,
                "contentCount", contentCount,
                "originalCount", features.size(),
                "status", "SUCCESS"
            );

            return ResponseEntity.ok(CommonResponse.success("중복 제거 결과가 성공적으로 저장되었습니다.", result));

        } catch (Exception e) {
            log.error("중복 제거 결과 저장 실패", e);
            return ResponseEntity.internalServerError()
                .body(CommonResponse.error("DEDUP_SAVE_ERROR", "중복 제거 결과 저장 실패: " + e.getMessage()));
        }
    }

    /**
     * 메인 DB에 저장된 특징 통계 조회 (Sub 백엔드에서 호출 가능)
     */
    @GetMapping("/statistics")
    public ResponseEntity<CommonResponse<Map<String, Object>>> getMainDbFeatureStatistics() {
        try {
            long totalCount = coverLetterFeatureRepository.count();
            long expressionCount = coverLetterFeatureRepository.countByFeaturesCategory(FeaturesCategory.EXPRESSION);
            long structureCount = coverLetterFeatureRepository.countByFeaturesCategory(FeaturesCategory.STRUCTURE);
            long contentCount = coverLetterFeatureRepository.countByFeaturesCategory(FeaturesCategory.CONTENT);

            Map<String, Object> statistics = Map.of(
                "totalCount", totalCount,
                "expressionCount", expressionCount,
                "structureCount", structureCount,
                "contentCount", contentCount
            );

            log.info("메인 DB 특징 통계 조회 - 총 {}개 특징", totalCount);

            return ResponseEntity.ok(CommonResponse.success("메인 DB 특징 통계", statistics));
        } catch (Exception e) {
            log.error("메인 DB 특징 통계 조회 실패", e);
            return ResponseEntity.internalServerError()
                .body(CommonResponse.error("STATISTICS_ERROR", "통계 조회 실패: " + e.getMessage()));
        }
    }
}
