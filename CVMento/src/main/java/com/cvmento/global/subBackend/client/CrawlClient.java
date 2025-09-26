package com.cvmento.global.subBackend.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 크롤링 데이터 관련 Sub Backend 클라이언트
 * 크롤링된 자소서 데이터의 CRUD 작업을 담당
 */
@FeignClient(
        name = "sub-backend-crawl",
        url = "${sub-backend.url}",
        configuration = SubBackendFeignConfig.class
)
public interface CrawlClient {

    /**
     * 크롤링 데이터 페이징 조회
     */
    @GetMapping("/api/internal/crawl")
    Page<Map<String, Object>> getCrawlData(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort
    );

    /**
     * 크롤링 데이터 단건 조회
     */
    @GetMapping("/api/internal/crawl/{id}")
    Map<String, Object> getCrawlDataById(@PathVariable Long id);

    /**
     * 크롤링 데이터 수정
     */
    @PutMapping("/api/internal/crawl/{id}")
    Map<String, Object> updateCrawlData(@PathVariable Long id, @RequestBody Map<String, Object> request);

    /**
     * 크롤링 데이터 단건 삭제
     */
    @DeleteMapping("/api/internal/crawl/{id}")
    void deleteCrawlData(@PathVariable Long id);

    /**
     * 크롤링 데이터 전체 삭제
     */
    @DeleteMapping("/api/internal/crawl")
    void deleteAllCrawlData();
}
