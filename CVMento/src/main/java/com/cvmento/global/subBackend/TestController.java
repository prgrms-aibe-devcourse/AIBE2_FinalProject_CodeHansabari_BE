package com.cvmento.global.subBackend;

import com.cvmento.global.subBackend.client.SubBackendClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
@Slf4j
public class TestController {

    private final SubBackendClient subBackendClient;

    @PostMapping("/sub-backend-connection")
    public ResponseEntity<Map<String, Object>> testSubBackendConnection() {
        try {
            Map<String, Object> testData = Map.of(
                    "message", "Hello from main backend",
                    "timestamp", System.currentTimeMillis()
            );

            ResponseEntity<Map<String, Object>> response = subBackendClient.test(testData);

            log.info("Sub backend response: {}", response.getBody());

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Sub backend communication successful",
                    "subBackendResponse", response.getBody()
            ));

        } catch (Exception e) {
            log.error("Sub backend communication failed", e);

            return ResponseEntity.ok(Map.of(
                    "success", false,
                    "message", "Sub backend communication failed: " + e.getMessage()
            ));
        }
    }

    @PostMapping("/job-flow")
    public ResponseEntity<Map<String, Object>> testJobFlow() {
        try {
            String jobId = UUID.randomUUID().toString();

            // 1. 작업 시작 요청
            Map<String, Object> jobData = Map.of(
                    "jobId", jobId,
                    "userId", 123L,
                    "data", "test analysis data"
            );

            ResponseEntity<Map<String, Object>> startResponse = subBackendClient.startJob(jobData);
            log.info("Job start response: {}", startResponse.getBody());

            // 2. 잠시 후 상태 조회
            Thread.sleep(1000);

            ResponseEntity<Map<String, Object>> statusResponse = subBackendClient.getJobStatus(jobId);
            log.info("Job status response: {}", statusResponse.getBody());

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "jobId", jobId,
                    "startResponse", startResponse.getBody(),
                    "statusResponse", statusResponse.getBody()
            ));

        } catch (Exception e) {
            log.error("Job flow test failed", e);

            return ResponseEntity.ok(Map.of(
                    "success", false,
                    "message", "Job flow test failed: " + e.getMessage()
            ));
        }
    }
}