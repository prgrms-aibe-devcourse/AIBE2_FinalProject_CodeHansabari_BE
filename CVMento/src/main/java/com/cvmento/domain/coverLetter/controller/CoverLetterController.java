package com.cvmento.domain.coverLetter.controller;

import com.cvmento.domain.coverLetter.controller.interfaces.CoverLetterControllerInterface;
import com.cvmento.domain.coverLetter.dto.request.CoverLetterSaveRequest;
import com.cvmento.domain.coverLetter.dto.request.CoverLetterUpdateRequest;
import com.cvmento.domain.coverLetter.dto.response.CoverLetterDetailResponse;
import com.cvmento.domain.coverLetter.dto.response.CoverLetterListResponse;
import com.cvmento.domain.coverLetter.dto.response.CoverLetterStatusListResponse;
import com.cvmento.domain.coverLetter.enums.CoverLetterStatus;
import com.cvmento.domain.coverLetter.service.CoverLetterService;
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

/**
 * 자소서 CRUD API
 */
@RestController
@RequestMapping("/api/v1/cover-letters")
@RequiredArgsConstructor
@Slf4j
public class CoverLetterController implements CoverLetterControllerInterface {

    private final CoverLetterService coverLetterService;

    /**
     * 자소서 저장
     */
    @PostMapping
    @Override
    public ResponseEntity<CommonResponse<Void>> saveCoverLetter(
            @Valid @RequestBody CoverLetterSaveRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        MDC.put("spanId", "coverletter-save-controller");

        String memberEmail = userDetails.getUsername();
        coverLetterService.saveCoverLetter(request, memberEmail);
        log.info("자소서 저장 요청 - 타입: {}, 제목: {}, 지원분야: {}, 경력: {}년",
                request.isAiImproved() ? "AI첨삭" : "원본", request.title(), request.jobField(), request.experienceYears());

        String message = request.isAiImproved() ?
                "AI 첨삭된 자소서가 저장되었습니다." :
                "원본 자소서가 저장되었습니다.";

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(CommonResponse.success(message, null));
    }

    /**
     * 자소서 수정
     */
    @PutMapping("/{coverLetterId}")
    @Override
    public ResponseEntity<CommonResponse<Void>> updateCoverLetter(
            @PathVariable Long coverLetterId,
            @Valid @RequestBody CoverLetterUpdateRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        MDC.put("spanId", "coverletter-update-controller");
        String memberEmail = userDetails.getUsername();
        log.info("자소서 수정 요청 - coverLetterId: {}, 제목: {}",
                coverLetterId, request.title());
        coverLetterService.updateCoverLetter(coverLetterId, request, memberEmail);

        return ResponseEntity.ok(CommonResponse.success("자소서가 수정되었습니다.", null));
    }

    /**
     * 자소서 삭제(소프트 삭제)
     */
    @DeleteMapping("/{coverLetterId}")
    @Override
    public ResponseEntity<CommonResponse<Void>> deleteCoverLetter(
            @PathVariable Long coverLetterId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        MDC.put("spanId", "coverletter-delete-controller");
        String memberEmail = userDetails.getUsername();

        log.info("자소서 삭제 요청 - coverLetterId: {}", coverLetterId);
        coverLetterService.deleteCoverLetter(coverLetterId, memberEmail);

        return ResponseEntity.ok(CommonResponse.success("자소서가 삭제되었습니다.", null));
    }

    /**
     * 자소서 목록 조회(페이징, 썸네일/전체 보기)
     */
    @GetMapping
    @Override
    public ResponseEntity<CommonResponse<Page<CoverLetterListResponse>>> getCoverLetters(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(required = false) String view,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        MDC.put("spanId", "coverletter-list-controller");

        String memberEmail = userDetails.getUsername();
        log.info("자소서 목록 조회 요청 - page: {}, size: {}, view: {}", page, size, view);

        Pageable pageable = PageRequest.of(page, size);
        Page<CoverLetterListResponse> response = coverLetterService.getCoverLetters(memberEmail, pageable, view);

        return ResponseEntity.ok(CommonResponse.success("자소서 목록 조회 성공", response));
    }

    /**
     * 자소서 상세 조회
     */
    @GetMapping("/{coverLetterId}")
    @Override
    public ResponseEntity<CommonResponse<CoverLetterDetailResponse>> getCoverLetter(
            @PathVariable Long coverLetterId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        MDC.put("spanId", "coverletter-detail-controller");

        String memberEmail = userDetails.getUsername();
        log.info("자소서 상세 조회 요청 - coverLetterId: {}", coverLetterId);

        CoverLetterDetailResponse response = coverLetterService.getCoverLetter(coverLetterId, memberEmail);

        return ResponseEntity.ok(CommonResponse.success("자소서 조회 성공", response));
    }

    /**
     * 자소서 복구(관리자 전용)
     */
    @PatchMapping("/{coverLetterId}/restore")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ROOT')")
    @Override
    public ResponseEntity<CommonResponse<Void>> restoreCoverLetter(
            @PathVariable Long coverLetterId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        MDC.put("spanId", "coverletter-restore-controller");
        String adminEmail = userDetails.getUsername();
        log.info("관리자 자소서 복구 요청 - coverLetterId: {}", coverLetterId);
        coverLetterService.restoreCoverLetter(coverLetterId, adminEmail);

        return ResponseEntity.ok(CommonResponse.success("자소서가 복구되었습니다.", null));
    }
    /**
     * 삭제된 자소서 목록 조회 (관리자 전용)
     */
    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN') or hasRole('ROOT')")
    @Override
    public ResponseEntity<CommonResponse<Page<CoverLetterStatusListResponse>>> getDeletedCoverLetters(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "DELETED") String status,
            @RequestParam(required = false) @Size(max = 320, message = "이메일은 320자를 초과할 수 없습니다.") String email,
            @RequestParam(required = false) @Size(max = 100, message = "제목은 100자를 초과할 수 없습니다.") String title,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        MDC.put("spanId", "coverletter-status-list-controller");

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
}
