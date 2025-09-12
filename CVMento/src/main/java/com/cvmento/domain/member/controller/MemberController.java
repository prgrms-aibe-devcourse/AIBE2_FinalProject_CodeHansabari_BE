package com.cvmento.domain.member.controller;

import com.cvmento.domain.member.controller.interfaces.MemberControllerInterface;
import com.cvmento.domain.member.dto.request.MemberStatusUpdateRequest;
import com.cvmento.domain.member.dto.request.MemberRoleUpdateRequest;
import com.cvmento.domain.member.dto.response.MemberDetailResponse;
import com.cvmento.domain.member.dto.response.MemberListResponse;
import com.cvmento.domain.member.entity.Member;
import com.cvmento.domain.member.enums.Role;
import com.cvmento.domain.member.enums.UserStatus;
import com.cvmento.domain.member.service.MemberService;
import com.cvmento.domain.auth.service.AuthService;
import com.cvmento.global.common.dto.CommonResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@Tag(name = "회원 관리", description = "회원 관리 API (관리자용)")
@RestController
@RequestMapping("/api/v1/members")
@RequiredArgsConstructor
@Slf4j
public class MemberController implements MemberControllerInterface {

    private final MemberService memberService;
    private final AuthService authService;

    @GetMapping("/admin/list")
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

        validateAdminAccess(userDetails);

        Pageable pageable = PageRequest.of(page, size);
        Page<MemberListResponse> memberList = memberService.getMemberList(
                pageable, email, name, role, status, sortBy, sortDirection);

        log.info("관리자 {} - 회원 목록 조회: 페이지={}, 크기={}, 총 {}건",
                userDetails.getUsername(), page, size, memberList.getTotalElements());

        return ResponseEntity.ok(CommonResponse.success("회원 목록 조회 성공", memberList));
    }

    @GetMapping("/admin/{memberId}")
    @Override
    public ResponseEntity<CommonResponse<MemberDetailResponse>> getMemberDetail(
            @PathVariable Long memberId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        MDC.put("spanId", "member-detail-controller");

        validateAdminAccess(userDetails);

        MemberDetailResponse memberDetail = memberService.getMemberDetail(memberId);

        log.info("관리자 {} - 회원 상세 조회: memberId={}", userDetails.getUsername(), memberId);

        return ResponseEntity.ok(CommonResponse.success("회원 상세 조회 성공", memberDetail));
    }

    @PutMapping("/admin/{memberId}/status")
    @Override
    public ResponseEntity<CommonResponse<Void>> updateMemberStatus(
            @PathVariable Long memberId,
            @Valid @RequestBody MemberStatusUpdateRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        MDC.put("spanId", "member-status-update-controller");

        validateAdminAccess(userDetails);

        Member admin = authService.getMemberFromUserDetails(userDetails);
        memberService.updateMemberStatus(memberId, request, admin);

        log.info("관리자 {} - 회원 상태 변경: memberId={}, newStatus={}, reason={}",
                userDetails.getUsername(), memberId, request.status(), request.reason());

        return ResponseEntity.ok(CommonResponse.success("회원 상태가 변경되었습니다."));
    }

    @PutMapping("/admin/{memberId}/role")
    @Override
    public ResponseEntity<CommonResponse<Void>> updateMemberRole(
            @PathVariable Long memberId,
            @Valid @RequestBody MemberRoleUpdateRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        MDC.put("spanId", "member-role-update-controller");

        validateAdminAccess(userDetails);

        Member admin = authService.getMemberFromUserDetails(userDetails);
        memberService.updateMemberRole(memberId, request, admin);

        log.info("관리자 {} - 회원 역할 변경: memberId={}, newRole={}, reason={}",
                userDetails.getUsername(), memberId, request.role(), request.reason());

        return ResponseEntity.ok(CommonResponse.success("회원 역할이 변경되었습니다."));
    }

    @GetMapping("/admin/statistics")
    @Override
    public ResponseEntity<CommonResponse<Object>> getMemberStatistics(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        MDC.put("spanId", "member-statistics-controller");

        validateAdminAccess(userDetails);

        Object statistics = memberService.getMemberStatistics();

        log.info("관리자 {} - 회원 통계 조회", userDetails.getUsername());

        return ResponseEntity.ok(CommonResponse.success("회원 통계 조회 성공", statistics));
    }

    @PostMapping("/admin/{memberId}/force-logout")
    @Override
    public ResponseEntity<CommonResponse<Void>> forceMemberLogout(
            @PathVariable Long memberId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        MDC.put("spanId", "member-force-logout-controller");

        validateAdminAccess(userDetails);

        Member admin = authService.getMemberFromUserDetails(userDetails);
        memberService.forceMemberLogout(memberId, admin);

        log.info("관리자 {} - 회원 강제 로그아웃: memberId={}", userDetails.getUsername(), memberId);

        return ResponseEntity.ok(CommonResponse.success("회원이 강제 로그아웃되었습니다."));
    }

    /**
     * 관리자 권한 검증
     */
    private void validateAdminAccess(UserDetails userDetails) {
        if (userDetails == null) {
            throw new AccessDeniedException("인증되지 않은 사용자입니다.");
        }

        Member member = authService.getMemberFromUserDetails(userDetails);
        if (member.getRole() != Role.ADMIN && member.getRole() != Role.ROOT) {
            throw new AccessDeniedException("관리자 권한이 필요합니다.");
        }
    }
}