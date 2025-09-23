package com.cvmento.global.subBackend.client;

import com.cvmento.global.common.dto.CommonResponse;
import com.cvmento.global.subBackend.dto.response.StepStatusResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@FeignClient(
        name = "sub-backend",
        url = "${sub-backend.url}",
        configuration = SubBackendFeignConfig.class
)
public interface SubBackendClient {

    @PostMapping("/api/internal/test")
    ResponseEntity<Map<String, Object>> test(@RequestBody Map<String, Object> request);

    @PostMapping("/api/internal/job-start")
    ResponseEntity<Map<String, Object>> startJob(@RequestBody Map<String, Object> request);

    @GetMapping("/api/internal/job-status/{jobId}")
    ResponseEntity<Map<String, Object>> getJobStatus(@PathVariable String jobId);

    @GetMapping("/api/internal/analysis-steps")
    ResponseEntity<CommonResponse<StepStatusResponse>> getOverallStatus();

    @GetMapping("/api/internal/analysis-steps")
    ResponseEntity<CommonResponse<StepStatusResponse>> getStepStatus(@RequestParam String step);

}