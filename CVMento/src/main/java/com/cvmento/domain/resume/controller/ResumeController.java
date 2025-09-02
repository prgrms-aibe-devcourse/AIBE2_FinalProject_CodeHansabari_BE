package com.cvmento.domain.resume.controller;

import com.cvmento.domain.resume.dto.request.ResumeCreateRequest;
import com.cvmento.domain.resume.dto.request.ResumeUpdateRequest;
import com.cvmento.domain.resume.dto.response.ResumeResponse;
import com.cvmento.domain.resume.service.ResumeService;
import com.cvmento.global.common.dto.CommonResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/resumes")
public class ResumeController {

    private final ResumeService resumeService;

    @PostMapping
    public ResponseEntity<CommonResponse<ResumeResponse>> createResume(@RequestBody ResumeCreateRequest resumeCreateRequest) {
        ResumeResponse resumeResponse = resumeService.createResume(resumeCreateRequest);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(CommonResponse.success("이력서 생성 성공", resumeResponse));
    }

    @GetMapping("/{resumeId}")
    public ResponseEntity<CommonResponse<ResumeResponse>> getResumeById(@PathVariable Long resumeId) {
        ResumeResponse resumeResponse = resumeService.getResume(resumeId);
        return ResponseEntity.ok(CommonResponse.success("이력서 조회 성공", resumeResponse));
    }

    @GetMapping
    public ResponseEntity<CommonResponse<List<ResumeResponse>>> getResumesByUser() {
        // TODO: Get memberId from SecurityContext
        List<ResumeResponse> resumes = resumeService.getResumesByMember(1L);
        return ResponseEntity.ok(CommonResponse.success("이력서 목록 조회 성공", resumes));
    }

    @PutMapping("/{resumeId}")
    public ResponseEntity<CommonResponse<ResumeResponse>> updateResume(@PathVariable Long resumeId, @RequestBody ResumeUpdateRequest request) {
        ResumeResponse resumeResponse = resumeService.updateResume(resumeId, request);
        return ResponseEntity.ok(CommonResponse.success("이력서 수정 성공", resumeResponse));
    }

    @DeleteMapping("/{resumeId}")
    public ResponseEntity<CommonResponse<Void>> deleteResume(@PathVariable Long resumeId) {
        resumeService.deleteResume(resumeId);
        return ResponseEntity.ok(CommonResponse.success("이력서 삭제 성공"));
    }
}