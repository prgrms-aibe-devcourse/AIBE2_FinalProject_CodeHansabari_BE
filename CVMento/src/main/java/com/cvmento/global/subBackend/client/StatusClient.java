package com.cvmento.global.subBackend.client;

import com.cvmento.global.common.dto.CommonResponse;
import com.cvmento.global.subBackend.dto.response.StepStatusResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * 상태 조회 관련 Sub Backend 클라이언트
 * Sub Backend의 전체 상태 및 개별 단계별 상태 조회를 담당
 */
@FeignClient(
        name = "sub-backend-status",
        url = "${sub-backend.url}",
        configuration = SubBackendFeignConfig.class
)
public interface StatusClient {

    /**
     * Sub Backend 전체 상태 조회
     */
    @GetMapping("/api/internal/status/overall")
    ResponseEntity<CommonResponse<StepStatusResponse>> getOverallStatus();

    /**
     * Sub Backend 특정 단계 상태 조회
     * @param step 조회할 단계 (예: "crawling", "feature-extraction", "deduplication")
     */
    @GetMapping("/api/internal/status/step/{step}")
    ResponseEntity<CommonResponse<StepStatusResponse>> getStepStatus(@PathVariable String step);
}
