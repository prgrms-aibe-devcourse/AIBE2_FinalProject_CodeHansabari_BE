package com.cvmento.domain.member.controller;

import com.cvmento.domain.auth.service.AuthService;
import com.cvmento.domain.coverLetter.dto.response.CoverLetterStatusListResponse;
import com.cvmento.domain.coverLetter.enums.CoverLetterStatus;
import com.cvmento.domain.coverLetter.service.CoverLetterService;
import com.cvmento.domain.member.controller.interfaces.AdminControllerInterface;
import com.cvmento.domain.member.dto.request.MemberRoleUpdateRequest;
import com.cvmento.domain.member.dto.request.MemberStatusUpdateRequest;
import com.cvmento.domain.member.dto.response.MemberDetailResponse;
import com.cvmento.domain.member.dto.response.MemberListResponse;
import com.cvmento.domain.member.entity.Member;
import com.cvmento.domain.member.enums.Role;
import com.cvmento.domain.member.enums.UserStatus;
import com.cvmento.domain.member.service.MemberService;
import com.cvmento.domain.resume.dto.response.ResumeStatusListResponse;
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
@RestController
@RequestMapping("/api/v1/admin")
@PreAuthorize("hasRole('ADMIN') or hasRole('ROOT')")
@RequiredArgsConstructor
@Slf4j
public class AdminController implements AdminControllerInterface {

    private final CoverLetterService coverLetterService;
    private final ResumeService resumeService;
    private final MemberService memberService;
    private final AuthService authService;

    /**
     * 상태별 자소서 목록 조회 (관리자 전용)
     */
    @GetMapping("/cover-letters")
    @Override
    public ResponseEntity<CommonResponse<Page<CoverLetterStatusListResponse>>> getCoverLetters(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "DELETED") String status,
            @RequestParam(required = false) @Size(max = 320, message = "이메일은 320자를 초과할 수 없습니다.") String email,
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
    @PatchMapping("/cover-letters/{coverLetterId}/restore")
    @Override
    public ResponseEntity<CommonResponse<Void>> restoreCoverLetter(
            @PathVariable Long coverLetterId,
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
    @GetMapping("/resumes")
    @Override
    public ResponseEntity<CommonResponse<Page<ResumeStatusListResponse>>> getResumes(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "DELETED") String status,
            @RequestParam(required = false) @Size(max = 320, message = "이메일은 320자를 초과할 수 없습니다.") String email,
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
    @PatchMapping("/resumes/{resumeId}/restore")
    @Override
    public ResponseEntity<CommonResponse<Void>> restoreResume(
            @PathVariable Long resumeId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        MDC.put("spanId", "admin-resume-restore-controller");
        String adminEmail = userDetails.getUsername();
        log.info("관리자 이력서 복구 요청 - resumeId: {}", resumeId);
        resumeService.restoreResume(resumeId, adminEmail);

        return ResponseEntity.ok(CommonResponse.success("이력서가 복구되었습니다.", null));
    }

    /**
     * 회원 목록 조회 (관리자 전용)
     */
    @GetMapping
    @Override
    public ResponseEntity<CommonResponse<Page<MemberListResponse>>> getMemberList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Role role,
            @RequestParam(required = false) UserStatus status,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        MDC.put("spanId", "member-list-controller");

        Pageable pageable = PageRequest.of(page, size);
        Page<MemberListResponse> memberList = memberService.getMemberList(
                pageable, email, name, role, status, sortBy, sortDirection);

        log.info("관리자 {} - 회원 목록 조회: 페이지={}, 크기={}, 총 {}건",
                userDetails.getUsername(), page, size, memberList.getTotalElements());

        return ResponseEntity.ok(CommonResponse.success("회원 목록 조회 성공", memberList));
    }

    /**
     * 회원 상세 조회 (관리자 전용)
     */
    @GetMapping("/{memberId}")
    @Override
    public ResponseEntity<CommonResponse<MemberDetailResponse>> getMemberDetail(
            @PathVariable Long memberId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        MDC.put("spanId", "member-detail-controller");

        MemberDetailResponse memberDetail = memberService.getMemberDetail(memberId);

        log.info("관리자 {} - 회원 상세 조회: memberId={}", userDetails.getUsername(), memberId);

        return ResponseEntity.ok(CommonResponse.success("회원 상세 조회 성공", memberDetail));
    }

    /**
     * 회원 상태 변경 (관리자 전용)
     */
    @PutMapping("/{memberId}/status")
    @Override
    public ResponseEntity<CommonResponse<Void>> updateMemberStatus(
            @PathVariable Long memberId,
            @Valid @RequestBody MemberStatusUpdateRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        MDC.put("spanId", "member-status-update-controller");

        Member admin = authService.getMemberFromUserDetails(userDetails);
        memberService.updateMemberStatus(memberId, request, admin);

        log.info("관리자 {} - 회원 상태 변경: memberId={}, newStatus={}, reason={}",
                userDetails.getUsername(), memberId, request.status(), request.reason());

        return ResponseEntity.ok(CommonResponse.success("회원 상태가 변경되었습니다."));
    }

    /**
     * 회원 역할 변경 (관리자 전용)
     */
    @PutMapping("/{memberId}/role")
    @Override
    public ResponseEntity<CommonResponse<Void>> updateMemberRole(
            @PathVariable Long memberId,
            @Valid @RequestBody MemberRoleUpdateRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        MDC.put("spanId", "member-role-update-controller");

        Member admin = authService.getMemberFromUserDetails(userDetails);
        memberService.updateMemberRole(memberId, request, admin);

        log.info("관리자 {} - 회원 역할 변경: memberId={}, newRole={}, reason={}",
                userDetails.getUsername(), memberId, request.role(), request.reason());

        return ResponseEntity.ok(CommonResponse.success("회원 역할이 변경되었습니다."));
    }

    /**
     * 회원 통계 조회 (관리자 전용)
     */
    @GetMapping("/statistics")
    @Override
    public ResponseEntity<CommonResponse<Object>> getMemberStatistics(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        MDC.put("spanId", "member-statistics-controller");

        Object statistics = memberService.getMemberStatistics();

        log.info("관리자 {} - 회원 통계 조회", userDetails.getUsername());

        return ResponseEntity.ok(CommonResponse.success("회원 통계 조회 성공", statistics));
    }

    /**
     * 회원 강제 로그아웃 (관리자 전용)
     */
    @PostMapping("/{memberId}/force-logout")
    @Override
    public ResponseEntity<CommonResponse<Void>> forceMemberLogout(
            @PathVariable Long memberId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        MDC.put("spanId", "member-force-logout-controller");

        Member admin = authService.getMemberFromUserDetails(userDetails);
        memberService.forceMemberLogout(memberId, admin);

        log.info("관리자 {} - 회원 강제 로그아웃: memberId={}", userDetails.getUsername(), memberId);

        return ResponseEntity.ok(CommonResponse.success("회원이 강제 로그아웃되었습니다."));
    }
}