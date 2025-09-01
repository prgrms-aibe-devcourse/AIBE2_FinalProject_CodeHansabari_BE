package com.cvmento.domain.resume.controller;

import com.cvmento.domain.resume.dto.request.ResumeCreateRequest;
import com.cvmento.domain.resume.dto.request.ResumeUpdateRequest;
import com.cvmento.domain.resume.dto.response.ResumeResponse;
import com.cvmento.domain.resume.service.ResumeService;
import com.cvmento.global.common.dto.CommonResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/resumes")
public class ResumeController {

    private final ResumeService resumeService;

    @PostMapping
    public ResponseEntity<CommonResponse<ResumeResponse>> createResume(
            @Valid @RequestBody ResumeCreateRequest resumeCreateRequest,
            @AuthenticationPrincipal UserDetails userDetails) {
        ResumeResponse resumeResponse = resumeService.createResume(resumeCreateRequest, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(CommonResponse.success("이력서 생성 성공", resumeResponse));
    }

    @GetMapping("/{resumeId}")
    public ResponseEntity<CommonResponse<ResumeResponse>> getResumeById(
            @PathVariable Long resumeId,
            @AuthenticationPrincipal UserDetails userDetails) {
        ResumeResponse resumeResponse = resumeService.getResume(resumeId, userDetails.getUsername());
        return ResponseEntity.ok(CommonResponse.success("이력서 조회 성공", resumeResponse));
    }

    @GetMapping
    public ResponseEntity<CommonResponse<List<ResumeResponse>>> getResumesByUser(@AuthenticationPrincipal UserDetails userDetails) {
        List<ResumeResponse> resumes = resumeService.getResumesByMember(userDetails.getUsername());
        return ResponseEntity.ok(CommonResponse.success("이력서 목록 조회 성공", resumes));
    }

    @PutMapping("/{resumeId}")
    public ResponseEntity<CommonResponse<ResumeResponse>> updateResume(
            @PathVariable Long resumeId,
            @Valid @RequestBody ResumeUpdateRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        ResumeResponse resumeResponse = resumeService.updateResume(resumeId, request, userDetails.getUsername());
        return ResponseEntity.ok(CommonResponse.success("이력서 수정 성공", resumeResponse));
    }

    @DeleteMapping("/{resumeId}")
    public ResponseEntity<CommonResponse<Void>> deleteResume(
            @PathVariable Long resumeId,
            @AuthenticationPrincipal UserDetails userDetails) {
        resumeService.deleteResume(resumeId, userDetails.getUsername());
        return ResponseEntity.ok(CommonResponse.success("이력서 삭제 성공"));
    }
}