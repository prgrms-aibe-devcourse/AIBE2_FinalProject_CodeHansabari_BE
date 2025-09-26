package com.cvmento.global.subBackend.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 특징 조회 관련 Sub Backend 클라이언트
 * 특징 데이터의 조회, 통계 등의 읽기 전용 작업을 담당
 */
@FeignClient(
        name = "sub-backend-feature",
        url = "${sub-backend.url}",
        configuration = SubBackendFeignConfig.class
)
public interface FeatureClient {

    /**
     * 특징 통계 조회
     */
    @GetMapping("/api/internal/features/statistics")
    ResponseEntity<Map<String, Object>> getFeatureStatistics();

    /**
     * 모든 특징 페이징 조회
     */
    @GetMapping("/api/internal/features")
    Page<Map<String, Object>> getAllFeatures(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort
    );

    /**
     * 카테고리별 특징 페이징 조회
     */
    @GetMapping("/api/internal/features/category/{category}")
    Page<Map<String, Object>> getFeaturesByCategory(
            @PathVariable String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "duplicateCount,desc") String sort
    );

    /**
     * Raw 특징 페이징 조회
     */
    @GetMapping("/api/internal/features/raw")
    Page<Map<String, Object>> getRawFeatures(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort
    );

    /**
     * 카테고리별 Raw 특징 페이징 조회
     */
    @GetMapping("/api/internal/features/raw/category/{category}")
    Page<Map<String, Object>> getRawFeaturesByCategory(
            @PathVariable String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort
    );
}
