package com.cvmento.domain.resume.controller;

import com.cvmento.domain.resume.controller.interfaces.ResumeMetadataControllerInterface;
import com.cvmento.domain.resume.dto.response.ResumeMetadataResponse;
import com.cvmento.domain.resume.service.ResumeMetadataService;
import com.cvmento.global.common.dto.CommonResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/resume-metadata")
@RequiredArgsConstructor
@Slf4j
public class ResumeMetadataController implements ResumeMetadataControllerInterface {

    private final ResumeMetadataService resumeMetadataService;

    /**
     * 이력서 작성용 메타데이터 조회
     */
    @GetMapping
    @Override
    public ResponseEntity<CommonResponse<ResumeMetadataResponse>> getResumeMetadata() {
        MDC.put("spanId", "metadata-controller");

        log.info("이력서 메타데이터 조회 요청");

        ResumeMetadataResponse metadata = resumeMetadataService.getResumeMetadata();

        return ResponseEntity.ok(CommonResponse.success("이력서 메타데이터를 성공적으로 조회했습니다.", metadata));
    }
}