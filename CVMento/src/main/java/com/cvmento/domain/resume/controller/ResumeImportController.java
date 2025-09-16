package com.cvmento.domain.resume.controller;

import com.cvmento.domain.resume.controller.interfaces.ResumeImportControllerInterface;
import com.cvmento.domain.resume.dto.response.ResumeImportResponse;
import com.cvmento.domain.resume.service.ResumeImportService;
import com.cvmento.global.common.dto.CommonResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/resume-import")
@RequiredArgsConstructor
@Slf4j
public class ResumeImportController implements ResumeImportControllerInterface {

    private final ResumeImportService resumeImportService;

    /**
     * 이력서 파일 변환
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Override
    public ResponseEntity<CommonResponse<ResumeImportResponse>> importResume(
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        MDC.put("spanId", "resume-import-controller");

        String memberEmail = userDetails.getUsername();

        log.info("이력서 변환 요청 - 사용자: {}, 파일명: {}, 크기: {}bytes",
                memberEmail, file.getOriginalFilename(), file.getSize());

        ResumeImportResponse response = resumeImportService.importResume(file, memberEmail);

        log.info("이력서 변환 완료 - 사용자: {}, 변환결과: {}",
                memberEmail, response.name());

        return ResponseEntity.ok(
                CommonResponse.success("이력서가 성공적으로 변환되었습니다.", response)
        );
    }
}