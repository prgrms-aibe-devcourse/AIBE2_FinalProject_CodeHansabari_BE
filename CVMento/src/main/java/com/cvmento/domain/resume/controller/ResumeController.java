package com.cvmento.domain.resume.controller;

import com.cvmento.domain.resume.controller.interfaces.ResumeControllerInterface;
import com.cvmento.domain.resume.dto.request.ResumeSaveRequest;
import com.cvmento.domain.resume.dto.request.ResumeUpdateRequest;
import com.cvmento.domain.resume.dto.response.ResumeDetailResponse;
import com.cvmento.domain.resume.dto.response.ResumeStatusListResponse;
import com.cvmento.domain.resume.dto.response.ResumeThumbnailResponse;
import com.cvmento.domain.resume.enums.ResumeStatus;
import com.cvmento.domain.resume.service.ResumeService;
import com.cvmento.global.common.dto.CommonResponse;
import com.cvmento.global.exception.customException.InvalidStatusException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/resumes")
@RequiredArgsConstructor
@Slf4j
public class ResumeController implements ResumeControllerInterface {

    private final ResumeService resumeService;

    /**
     * 이력서 저장
     */
    @PostMapping
    @Override
    public ResponseEntity<CommonResponse<Void>> saveResume(
            @Valid @RequestBody ResumeSaveRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        MDC.put("spanId", "resume-save-controller");

        String memberEmail = userDetails.getUsername();

        log.info("이력서 저장 요청 - 제목: {}, 타입: {}, 필드: {}",
                request.title(), request.type(), request.fieldName());

        resumeService.saveResume(request, memberEmail);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(CommonResponse.success("이력서가 성공적으로 저장되었습니다.", null));
    }

    /**
     * 이력서 수정
     */
    @PutMapping("/{resumeId}")
    @Override
    public ResponseEntity<CommonResponse<Void>> updateResume(
            @PathVariable Long resumeId,
            @Valid @RequestBody ResumeUpdateRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        MDC.put("spanId", "resume-update-controller");

        String memberEmail = userDetails.getUsername();

        log.info("이력서 수정 요청 - ID: {}, 제목: {}, 타입: {}",
                resumeId, request.title(), request.type());

        resumeService.updateResume(resumeId, request, memberEmail);

        return ResponseEntity.ok(CommonResponse.success("이력서가 성공적으로 수정되었습니다.", null));
    }

    /**
     * 이력서 삭제
     */
    @DeleteMapping("/{resumeId}")
    @Override
    public ResponseEntity<CommonResponse<Void>> deleteResume(
            @PathVariable Long resumeId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        MDC.put("spanId", "resume-delete-controller");

        String memberEmail = userDetails.getUsername();

        log.info("이력서 삭제 요청 - ID: {}", resumeId);

        resumeService.deleteResume(resumeId, memberEmail);

        return ResponseEntity.ok(CommonResponse.success("이력서가 성공적으로 삭제되었습니다.", null));
    }

    /**
     * 이력서 목록 조회 (썸네일, 페이징)
     */
    @GetMapping
    @Override
    public ResponseEntity<CommonResponse<Page<ResumeThumbnailResponse>>> getResumeList(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        MDC.put("spanId", "resume-list-controller");

        String memberEmail = userDetails.getUsername();

        log.info("이력서 목록 조회 요청 - page: {}, size: {}", page, size);

        Pageable pageable = PageRequest.of(page, size);
        Page<ResumeThumbnailResponse> resumePage = resumeService.getResumeList(memberEmail, pageable);

        log.info("이력서 목록 조회 완료 - 총 개수: {}", resumePage.getTotalElements());

        return ResponseEntity.ok(CommonResponse.success("이력서 목록을 성공적으로 조회했습니다.", resumePage));
    }

    /**
     * 이력서 상세 조회
     */
    @GetMapping("/{resumeId}")
    @Override
    public ResponseEntity<CommonResponse<ResumeDetailResponse>> getResumeDetail(
            @PathVariable Long resumeId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        MDC.put("spanId", "resume-detail-controller");

        String memberEmail = userDetails.getUsername();

        log.info("이력서 상세 조회 요청 - ID: {}", resumeId);

        ResumeDetailResponse resumeDetail = resumeService.getResumeDetail(resumeId, memberEmail);

        return ResponseEntity.ok(CommonResponse.success("이력서를 성공적으로 조회했습니다.", resumeDetail));
    }

    /**
     * 이력서 복구 (소프트 삭제된 이력서만) - 관리자 권한
     */
    @PatchMapping("/{resumeId}/restore")
    @Override
    public ResponseEntity<CommonResponse<Void>> restoreResume(
            @PathVariable Long resumeId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        MDC.put("spanId", "resume-restore-controller");

        String adminEmail = userDetails.getUsername();

        log.info("이력서 복구 요청 - ID: {}", resumeId);

        resumeService.restoreResume(resumeId, adminEmail);

        return ResponseEntity.ok(CommonResponse.success("이력서가 복구되었습니다.", null));
    }

    /**
     * 상태별 이력서 목록 조회 (관리자 전용)
     */
    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ROOT')")
    @Override
    public ResponseEntity<CommonResponse<Page<ResumeStatusListResponse>>> getResumesByStatus(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "DELETED") String status,
            @RequestParam(required = false) @Size(max = 320, message = "이메일은 320자를 초과할 수 없습니다.") String email,
            @RequestParam(required = false) @Size(max = 100, message = "제목은 100자를 초과할 수 없습니다.") String title,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        MDC.put("spanId", "resume-status-list-controller");

        // 상태값 검증 및 변환
        ResumeStatus resumeStatus;
        try {
            resumeStatus = ResumeStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new InvalidStatusException("올바르지 않은 상태값입니다. ACTIVE 또는 DELETED만 허용됩니다.");
        }

        String adminEmail = userDetails.getUsername();
        log.info("관리자 이력서 목록 조회 요청 - 상태: {}, page: {}, size: {}, email: {}, title: {}",
                status, page, size, email, title);

        Pageable pageable = PageRequest.of(page, size);
        Page<ResumeStatusListResponse> response = resumeService
                .getResumesByStatus(resumeStatus, email, title, pageable, adminEmail);

        String message = resumeStatus == ResumeStatus.DELETED
                ? "삭제된 이력서 목록 조회 성공"
                : "이력서 목록 조회 성공";

        return ResponseEntity.ok(CommonResponse.success(message, response));
    }
}