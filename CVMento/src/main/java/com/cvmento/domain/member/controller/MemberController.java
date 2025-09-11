package com.cvmento.domain.member.controller;

import com.cvmento.domain.member.dto.request.MemberStatusUpdateRequest;
import com.cvmento.domain.member.dto.request.MemberRoleUpdateRequest;
import com.cvmento.domain.member.dto.response.MemberDetailResponse;
import com.cvmento.domain.member.dto.response.MemberListResponse;
import com.cvmento.domain.member.dto.MemberInfo;
import com.cvmento.domain.member.entity.Member;
import com.cvmento.domain.member.enums.Role;
import com.cvmento.domain.member.enums.UserStatus;
import com.cvmento.domain.member.service.MemberService;
import com.cvmento.domain.auth.service.AuthService;
import com.cvmento.global.common.dto.CommonResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
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

@Tag(name = "회원 관리", description = "회원 관리 API (일반 사용자 + 관리자)")
@RestController
@RequestMapping("/api/v1/members")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "cookieAuth")
public class MemberController {

    private final MemberService memberService;
    private final AuthService authService;

    // ================ 일반 사용자용 API ================

    @Operation(
            summary = "내 프로필 조회",
            description = "현재 로그인한 사용자의 프로필 정보를 조회합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "프로필 조회 성공",
                            content = @Content(
                                    schema = @Schema(implementation = CommonResponse.class),
                                    examples = @ExampleObject(
                                            name = "프로필 조회 성공",
                                            value = """
                        {
                          "success": true,
                          "message": "프로필 조회 성공",
                          "data": {
                            "memberId": 1,
                            "email": "user@example.com",
                            "name": "홍길동",
                            "picture": "https://example.com/profile.jpg"
                          }
                        }
                        """
                                    )
                            )
                    )
            }
    )
    @GetMapping("/me")
    public ResponseEntity<CommonResponse<MemberInfo>> getMyProfile(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        MDC.put("spanId", "member-profile-controller");

        Member member = authService.getMemberFromUserDetails(userDetails);
        MemberInfo memberInfo = MemberInfo.from(member);

        log.info("사용자 프로필 조회: {}", userDetails.getUsername());

        return ResponseEntity.ok(CommonResponse.success("프로필 조회 성공", memberInfo));
    }

    // ================ 관리자용 API ================

    @Operation(
            summary = "[관리자] 회원 목록 조회",
            description = "관리자가 전체 회원 목록을 페이징으로 조회합니다. 검색 조건과 정렬을 지원합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "회원 목록 조회 성공",
                            content = @Content(
                                    schema = @Schema(implementation = CommonResponse.class),
                                    examples = @ExampleObject(
                                            name = "회원 목록 조회 성공",
                                            value = """
                        {
                          "success": true,
                          "message": "회원 목록 조회 성공",
                          "data": {
                            "content": [
                              {
                                "memberId": 1,
                                "email": "user@example.com",
                                "name": "홍길동",
                                "picture": "https://example.com/profile.jpg",
                                "role": "USER",
                                "status": "ACTIVE",
                                "lastLoginAt": "2025-09-08T10:30:00",
                                "createdAt": "2025-09-01T09:00:00",
                                "updatedAt": "2025-09-08T10:30:00"
                              }
                            ],
                            "totalElements": 150,
                            "totalPages": 15,
                            "size": 10,
                            "number": 0
                          }
                        }
                        """
                                    )
                            )
                    ),
                    @ApiResponse(responseCode = "403", description = "권한 없음")
            }
    )
    @GetMapping("/admin/list")
    public ResponseEntity<CommonResponse<Page<MemberListResponse>>> getMemberList(
            @Parameter(description = "페이지 번호 (0부터 시작)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "이메일 검색") @RequestParam(required = false) String email,
            @Parameter(description = "이름 검색") @RequestParam(required = false) String name,
            @Parameter(description = "역할 필터 (USER, ADMIN, ROOT)") @RequestParam(required = false) Role role,
            @Parameter(description = "상태 필터 (ACTIVE, INACTIVE, SUSPENDED)") @RequestParam(required = false) UserStatus status,
            @Parameter(description = "정렬 기준 (createdAt, lastLoginAt, email)") @RequestParam(defaultValue = "createdAt") String sortBy,
            @Parameter(description = "정렬 방향 (asc, desc)") @RequestParam(defaultValue = "desc") String sortDirection,
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

    @Operation(
            summary = "[관리자] 회원 상세 조회",
            description = "특정 회원의 상세 정보를 조회합니다."
    )
    @GetMapping("/admin/{memberId}")
    public ResponseEntity<CommonResponse<MemberDetailResponse>> getMemberDetail(
            @Parameter(description = "회원 ID") @PathVariable Long memberId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        MDC.put("spanId", "member-detail-controller");

        validateAdminAccess(userDetails);

        MemberDetailResponse memberDetail = memberService.getMemberDetail(memberId);

        log.info("관리자 {} - 회원 상세 조회: memberId={}", userDetails.getUsername(), memberId);

        return ResponseEntity.ok(CommonResponse.success("회원 상세 조회 성공", memberDetail));
    }

    @Operation(
            summary = "[관리자] 회원 상태 변경",
            description = "회원의 상태를 변경합니다. (ACTIVE, INACTIVE, SUSPENDED)"
    )
    @PutMapping("/admin/{memberId}/status")
    public ResponseEntity<CommonResponse<Void>> updateMemberStatus(
            @Parameter(description = "회원 ID") @PathVariable Long memberId,
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

    @Operation(
            summary = "[관리자] 회원 역할 변경",
            description = "회원의 역할을 변경합니다. ROOT 관리자만 다른 관리자의 역할을 변경할 수 있습니다."
    )
    @PutMapping("/admin/{memberId}/role")
    public ResponseEntity<CommonResponse<Void>> updateMemberRole(
            @Parameter(description = "회원 ID") @PathVariable Long memberId,
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

    @Operation(
            summary = "[관리자] 회원 통계",
            description = "전체 회원 통계 정보를 조회합니다."
    )
    @GetMapping("/admin/statistics")
    public ResponseEntity<CommonResponse<Object>> getMemberStatistics(
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        MDC.put("spanId", "member-statistics-controller");

        validateAdminAccess(userDetails);

        Object statistics = memberService.getMemberStatistics();

        log.info("관리자 {} - 회원 통계 조회", userDetails.getUsername());

        return ResponseEntity.ok(CommonResponse.success("회원 통계 조회 성공", statistics));
    }

    @Operation(
            summary = "[관리자] 회원 강제 로그아웃",
            description = "특정 회원을 강제로 로그아웃시킵니다. 모든 세션과 토큰이 무효화됩니다."
    )
    @PostMapping("/admin/{memberId}/force-logout")
    public ResponseEntity<CommonResponse<Void>> forceMemberLogout(
            @Parameter(description = "회원 ID") @PathVariable Long memberId,
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