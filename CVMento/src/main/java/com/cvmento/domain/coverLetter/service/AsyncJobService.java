package com.cvmento.domain.coverLetter.service;

import com.cvmento.global.subBackend.client.JobClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AsyncJobService {

    private final JobClient jobClient;

    /**
     * 비동기 중복제거 작업 실행
     */
    @Async
    public void executeDeduplicationAsync(Map<String, Object> jobRequest, String userEmail) {
        try {
            log.info("비동기 중복제거 작업 시작 - 사용자: {}, jobId: {}", userEmail, jobRequest.get("jobId"));
            jobClient.startJob(jobRequest);
            log.info("비동기 중복제거 작업 완료 - 사용자: {}, jobId: {}", userEmail, jobRequest.get("jobId"));
        } catch (Exception e) {
            log.error("비동기 중복제거 작업 실패 - 사용자: {}, jobId: {}, 에러: {}",
                    userEmail, jobRequest.get("jobId"), e.getMessage(), e);
        }
    }
}
