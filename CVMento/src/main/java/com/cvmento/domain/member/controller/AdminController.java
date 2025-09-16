package com.cvmento.domain.member.controller;

import com.cvmento.domain.coverLetter.dto.response.CoverLetterStatusListResponse;
import com.cvmento.domain.coverLetter.enums.CoverLetterStatus;
import com.cvmento.domain.coverLetter.service.CoverLetterService;
import com.cvmento.domain.member.controller.interfaces.AdminControllerInterface;
import com.cvmento.domain.resume.dto.response.ResumeStatusListResponse;
import com.cvmento.domain.resume.enums.ResumeStatus;
import com.cvmento.domain.resume.service.ResumeService;
import com.cvmento.global.common.dto.CommonResponse;
import com.cvmento.global.exception.customException.InvalidStatusException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

/**
 * 관리자 전용 컨트롤러
 * - 자소서 관리
 * - 이력서 관리
 */
@Tag(name = "관리자 기능", description = "관리자 전용 API - 자소서, 이력서 관리")
@RestController
@RequestMapping("/api/v1/admin")
@PreAuthorize("hasRole('ADMIN') or hasRole('ROOT')")
@RequiredArgsConstructor
@Slf4j
public class AdminController implements AdminControllerInterface {

    private final CoverLetterService coverLetterService;
    private final ResumeService resumeService;

    /**
     * 상태별 자소서 목록 조회 (관리자 전용)
     */
    @Operation(
            summary = "상태별 자소서 목록 조회",
            description = "관리자가 상태(ACTIVE/DELETED)에 따른 자소서 목록을 필터링하여 조회합니다.",
            security = @SecurityRequirement(name = "cookieAuth"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "자소서 목록 조회 성공"),
                    @ApiResponse(responseCode = "403", description = "관리자 권한 필요"),
                    @ApiResponse(responseCode = "400", description = "잘못된 상태값")
            }
    )
    @GetMapping("/cover-letters")
    @Override
    public ResponseEntity<CommonResponse<Page<CoverLetterStatusListResponse>>> getCoverLetters(
            @Parameter(description = "페이지 번호", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기", example = "20")
            @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "자소서 상태", example = "DELETED")
            @RequestParam(defaultValue = "DELETED") String status,
            @Parameter(description = "작성자 이메일 필터")
            @RequestParam(required = false) @Size(max = 320, message = "이메일은 320자를 초과할 수 없습니다.") String email,
            @Parameter(description = "자소서 제목 필터")
            @RequestParam(required = false) @Size(max = 100, message = "제목은 100자를 초과할 수 없습니다.") String title,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        MDC.put("spanId", "admin-coverletter-list-controller");

        // 상태값 검증 및 변환
        CoverLetterStatus coverLetterStatus;
        try {
            coverLetterStatus = CoverLetterStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new InvalidStatusException("올바르지 않은 상태값입니다. ACTIVE 또는 DELETED만 허용됩니다.");
        }

        String adminEmail = userDetails.getUsername();
        log.info("관리자 자소서 목록 조회 요청 - 상태: {}, page: {}, size: {}, email: {}, title: {}",
                status, page, size, email, title);

        Pageable pageable = PageRequest.of(page, size);
        Page<CoverLetterStatusListResponse> response = coverLetterService
                .getCoverLettersByStatus(coverLetterStatus, email, title, pageable, adminEmail);

        String message = coverLetterStatus == CoverLetterStatus.DELETED
                ? "삭제된 자소서 목록 조회 성공"
                : "자소서 목록 조회 성공";

        return ResponseEntity.ok(CommonResponse.success(message, response));
    }

    /**
     * 자소서 복구 (관리자 전용)
     */
    @Operation(
            summary = "자소서 복구",
            description = "삭제된 자소서를 관리자 권한으로 복구합니다.",
            security = @SecurityRequirement(name = "cookieAuth")
    )
    @PatchMapping("/cover-letters/{coverLetterId}/restore")
    @Override
    public ResponseEntity<CommonResponse<Void>> restoreCoverLetter(
            @Parameter(description = "복구할 자소서 ID") @PathVariable Long coverLetterId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        MDC.put("spanId", "admin-coverletter-restore-controller");
        String adminEmail = userDetails.getUsername();
        log.info("관리자 자소서 복구 요청 - coverLetterId: {}", coverLetterId);
        coverLetterService.restoreCoverLetter(coverLetterId, adminEmail);

        return ResponseEntity.ok(CommonResponse.success("자소서가 복구되었습니다.", null));
    }

    /**
     * 상태별 이력서 목록 조회 (관리자 전용)
     */
    @Operation(
            summary = "상태별 이력서 목록 조회",
            description = "관리자가 상태(ACTIVE/DELETED)에 따른 이력서 목록을 필터링하여 조회합니다.",
            security = @SecurityRequirement(name = "cookieAuth"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "이력서 목록 조회 성공"),
                    @ApiResponse(responseCode = "403", description = "관리자 권한 필요"),
                    @ApiResponse(responseCode = "400", description = "잘못된 상태값")
            }
    )
    @GetMapping("/resumes")
    @Override
    public ResponseEntity<CommonResponse<Page<ResumeStatusListResponse>>> getResumes(
            @Parameter(description = "페이지 번호", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기", example = "20")
            @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "이력서 상태", example = "DELETED")
            @RequestParam(defaultValue = "DELETED") String status,
            @Parameter(description = "작성자 이메일 필터")
            @RequestParam(required = false) @Size(max = 320, message = "이메일은 320자를 초과할 수 없습니다.") String email,
            @Parameter(description = "이력서 제목 필터")
            @RequestParam(required = false) @Size(max = 100, message = "제목은 100자를 초과할 수 없습니다.") String title,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        MDC.put("spanId", "admin-resume-list-controller");

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

    /**
     * 이력서 복구 (관리자 전용)
     */
    @Operation(
            summary = "이력서 복구",
            description = "삭제된 이력서를 관리자 권한으로 복구합니다.",
            security = @SecurityRequirement(name = "cookieAuth")
    )
    @PatchMapping("/resumes/{resumeId}/restore")
    @Override
    public ResponseEntity<CommonResponse<Void>> restoreResume(
            @Parameter(description = "복구할 이력서 ID") @PathVariable Long resumeId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        MDC.put("spanId", "admin-resume-restore-controller");
        String adminEmail = userDetails.getUsername();
        log.info("관리자 이력서 복구 요청 - resumeId: {}", resumeId);
        resumeService.restoreResume(resumeId, adminEmail);

        return ResponseEntity.ok(CommonResponse.success("이력서가 복구되었습니다.", null));
    }
}