package com.cvmento.global.subBackend.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

/**
 * 테스트 및 통신 확인용 Sub Backend 클라이언트
 * Sub Backend 와의 연결 상태 확인 및 기본 통신 테스트를 담당
 */
@FeignClient(
        name = "sub-backend-test",
        url = "${sub-backend.url}",
        configuration = SubBackendFeignConfig.class
)
public interface TestClient {

    /**
     * Sub Backend 연결 테스트
     * @param testData 테스트용 데이터
     * @return 테스트 응답
     */
    @PostMapping("/api/internal/test")
    ResponseEntity<Map<String, Object>> test(@RequestBody Map<String, Object> testData);
}
