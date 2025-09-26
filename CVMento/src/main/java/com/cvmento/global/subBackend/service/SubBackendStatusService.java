package com.cvmento.global.subBackend.service;

import com.cvmento.global.common.dto.CommonResponse;
import com.cvmento.global.subBackend.client.StatusClient;
import com.cvmento.global.subBackend.dto.response.StepStatusResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubBackendStatusService {

    private final StatusClient statusClient;

    /**
     * 서브 백엔드 전체 상태 조회
     */
    public StepStatusResponse getSubBackendOverallStatus() {
        try {
            ResponseEntity<CommonResponse<StepStatusResponse>> response =
                    statusClient.getOverallStatus();

            if (response.getStatusCode().is2xxSuccessful() &&
                    response.getBody() != null &&
                    response.getBody().isSuccess()) {

                return response.getBody().getData();
            }

            log.warn("Sub backend status request failed: {}", response);
            return null;

        } catch (Exception e) {
            log.error("Failed to get sub backend overall status", e);
            return null;
        }
    }

    /**
     * 서브 백엔드 특정 단계 상태 조회
     */
    public StepStatusResponse getSubBackendStepStatus(String step) {
        try {
            ResponseEntity<CommonResponse<StepStatusResponse>> response =
                    statusClient.getStepStatus(step);

            if (response.getStatusCode().is2xxSuccessful() &&
                    response.getBody() != null &&
                    response.getBody().isSuccess()) {

                return response.getBody().getData();
            }

            log.warn("Sub backend step status request failed: {}", response);
            return null;

        } catch (Exception e) {
            log.error("Failed to get sub backend step status for: {}", step, e);
            return null;
        }
    }
}