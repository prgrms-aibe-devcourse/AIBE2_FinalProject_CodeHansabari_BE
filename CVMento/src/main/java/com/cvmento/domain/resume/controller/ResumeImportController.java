package com.cvmento.domain.resume.controller;

import com.cvmento.domain.resume.dto.response.ResumeResponse;
import com.cvmento.domain.resume.service.ResumeImportService;
import com.cvmento.global.common.dto.CommonResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "이력서 파일 가져오기", description = "파일(PDF, PNG)로부터 이력서 자동 생성 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/resumes/import")
public class ResumeImportController {

    private final ResumeImportService resumeImportService;

    @Operation(
            summary = "이력서 파일 업로드로 생성",
            description = "PDF 또는 이미지 파일을 업로드하여 이력서를 자동으로 생성합니다. 이 API는 Vision LLM을 사용하여 파일 내용을 분석하고 이력서 형식으로 변환합니다."
    )
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CommonResponse<ResumeResponse>> importResumeFromFile(
            @RequestPart("file") MultipartFile file,
            @AuthenticationPrincipal UserDetails userDetails) {
        ResumeResponse resumeResponse = resumeImportService.createResumeFromFile(file, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(CommonResponse.success("이력서 파일로부터 생성 성공", resumeResponse));
    }
}
