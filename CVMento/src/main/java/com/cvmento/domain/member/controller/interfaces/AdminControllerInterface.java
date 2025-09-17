package com.cvmento.domain.member.controller.interfaces;

import com.cvmento.domain.coverLetter.dto.response.CoverLetterStatusListResponse;
import com.cvmento.domain.member.dto.request.MemberRoleUpdateRequest;
import com.cvmento.domain.member.dto.request.MemberStatusUpdateRequest;
import com.cvmento.domain.member.dto.response.MemberDetailResponse;
import com.cvmento.domain.member.dto.response.MemberListResponse;
import com.cvmento.domain.member.enums.Role;
import com.cvmento.domain.member.enums.UserStatus;
import com.cvmento.domain.resume.dto.response.ResumeStatusListResponse;
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
import jakarta.validation.constraints.Size;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "관리자 기능", description = "관리자 전용 API - 자소서, 이력서 관리")
public interface AdminControllerInterface {

    @Operation(
            summary = "상태별 자소서 목록 조회 (관리자 전용)",
            description = "관리자가 상태(ACTIVE/DELETED)에 따른 자소서 목록을 필터링하여 조회합니다. 이메일과 제목으로 추가 필터링이 가능합니다.",
            security = @SecurityRequirement(name = "cookieAuth"),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "자소서 목록 조회 성공",
                            content = @Content(
                                    schema = @Schema(implementation = CommonResponse.class),
                                    examples = @ExampleObject(
                                            name = "삭제된 자소서 목록 조회 성공",
                                            value = """
                                                    {
                                                      "success": true,
                                                      "message": "삭제된 자소서 목록 조회 성공",
                                                      "data": {
                                                        "content": [
                                                          {
                                                            "coverLetterId": 15,
                                                            "authorEmail": "user@example.com",
                                                            "title": "[원본] 네이버 백엔드 개발자 지원",
                                                            "status": "DELETED",
                                                            "createdAt": "2025-09-01T10:30:00",
                                                            "updatedAt": "2025-09-15T14:20:00",
                                                            "deletedAt": "2025-09-15T14:20:00",
                                                            "scheduledDeletionDate": "2025-10-15T14:20:00"
                                                          }
                                                        ],
                                                        "totalElements": 45,
                                                        "totalPages": 3
                                                      },
                                                      "timestamp": "2025-09-16T14:30:00"
                                                    }
                                                    """
                                    )
                            )
                    ),
                    @ApiResponse(responseCode = "403", description = "관리자 권한 필요"),
                    @ApiResponse(responseCode = "400", description = "잘못된 상태값")
            }
    )
    ResponseEntity<CommonResponse<Page<CoverLetterStatusListResponse>>> getCoverLetters(
            @Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기", example = "20")
            @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "자소서 상태 (ACTIVE 또는 DELETED)", example = "DELETED")
            @RequestParam(defaultValue = "DELETED") String status,
            @Parameter(description = "작성자 이메일 필터 (부분 검색)")
            @RequestParam(required = false) @Size(max = 320, message = "이메일은 320자를 초과할 수 없습니다.") String email,
            @Parameter(description = "자소서 제목 필터 (부분 검색)")
            @RequestParam(required = false) @Size(max = 100, message = "제목은 100자를 초과할 수 없습니다.") String title,
            @AuthenticationPrincipal UserDetails userDetails
    );

    @Operation(
            summary = "자소서 복구 (관리자 전용)",
            description = "삭제된 자소서를 관리자 권한으로 복구합니다.",
            security = @SecurityRequirement(name = "cookieAuth"),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "자소서 복구 성공",
                            content = @Content(
                                    schema = @Schema(implementation = CommonResponse.class),
                                    examples = @ExampleObject(
                                            name = "자소서 복구 성공 응답",
                                            value = """
                                                    {
                                                      "success": true,
                                                      "message": "자소서가 복구되었습니다.",
                                                      "data": null,
                                                      "timestamp": "2024-01-15T14:30:00"
                                                    }
                                                    """
                                    )
                            )
                    ),
                    @ApiResponse(responseCode = "403", description = "관리자 권한 필요"),
                    @ApiResponse(responseCode = "404", description = "복구할 수 있는 자소서를 찾을 수 없음")
            }
    )
    ResponseEntity<CommonResponse<Void>> restoreCoverLetter(
            @Parameter(description = "복구할 자소서 ID") @PathVariable Long coverLetterId,
            @AuthenticationPrincipal UserDetails userDetails
    );

    @Operation(
            summary = "상태별 이력서 목록 조회 (관리자 전용)",
            description = "관리자가 상태(ACTIVE/DELETED)에 따른 이력서 목록을 필터링하여 조회합니다. 이메일과 제목으로 추가 필터링이 가능합니다.",
            security = @SecurityRequirement(name = "cookieAuth"),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "이력서 목록 조회 성공",
                            content = @Content(
                                    schema = @Schema(implementation = CommonResponse.class),
                                    examples = @ExampleObject(
                                            name = "삭제된 이력서 목록 조회 성공",
                                            value = """
                                                    {
                                                      "success": true,
                                                      "message": "삭제된 이력서 목록 조회 성공",
                                                      "data": {
                                                        "content": [
                                                          {
                                                            "resumeId": 1,
                                                            "authorEmail": "user@example.com",
                                                            "title": "네이버 백엔드 개발자 지원용 이력서",
                                                            "status": "DELETED",
                                                            "createdAt": "2024-01-10T10:30:00",
                                                            "updatedAt": "2024-01-15T14:30:00",
                                                            "deletedAt": "2024-01-15T14:30:00",
                                                            "scheduledDeletionDate": "2024-02-14T14:30:00"
                                                          }
                                                        ],
                                                        "totalPages": 1,
                                                        "totalElements": 1,
                                                        "size": 20,
                                                        "number": 0
                                                      },
                                                      "timestamp": "2024-01-15T14:30:00"
                                                    }
                                                    """
                                    )
                            )
                    ),
                    @ApiResponse(responseCode = "403", description = "관리자 권한 필요"),
                    @ApiResponse(responseCode = "400", description = "잘못된 상태값")
            }
    )
    ResponseEntity<CommonResponse<Page<ResumeStatusListResponse>>> getResumes(
            @Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기", example = "20")
            @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "이력서 상태 (ACTIVE 또는 DELETED)", example = "DELETED")
            @RequestParam(defaultValue = "DELETED") String status,
            @Parameter(description = "작성자 이메일 필터 (부분 검색)")
            @RequestParam(required = false) @Size(max = 320, message = "이메일은 320자를 초과할 수 없습니다.") String email,
            @Parameter(description = "이력서 제목 필터 (부분 검색)")
            @RequestParam(required = false) @Size(max = 100, message = "제목은 100자를 초과할 수 없습니다.") String title,
            @AuthenticationPrincipal UserDetails userDetails
    );

    @Operation(
            summary = "이력서 복구 (관리자 전용)",
            description = "삭제된 이력서를 관리자 권한으로 복구합니다.",
            security = @SecurityRequirement(name = "cookieAuth"),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "이력서 복구 성공",
                            content = @Content(
                                    schema = @Schema(implementation = CommonResponse.class),
                                    examples = @ExampleObject(
                                            name = "이력서 복구 성공 응답",
                                            value = """
                                                    {
                                                      "success": true,
                                                      "message": "이력서가 복구되었습니다.",
                                                      "data": null,
                                                      "timestamp": "2024-01-15T14:30:00"
                                                    }
                                                    """
                                    )
                            )
                    ),
                    @ApiResponse(responseCode = "403", description = "관리자 권한 필요"),
                    @ApiResponse(responseCode = "404", description = "복구할 수 있는 이력서를 찾을 수 없음")
            }
    )
    ResponseEntity<CommonResponse<Void>> restoreResume(
            @Parameter(description = "복구할 이력서 ID") @PathVariable Long resumeId,
            @AuthenticationPrincipal UserDetails userDetails
    );

    @Operation(
            summary = "[관리자] 회원 목록 조회",
            description = """
                    관리자가 전체 회원 목록을 페이징으로 조회합니다. 
                    
                    **주요 기능:**
                    - 이메일, 이름으로 검색 가능
                    - 역할(USER, ADMIN, ROOT) 필터링
                    - 상태(ACTIVE, INACTIVE, SUSPENDED) 필터링
                    - 생성일, 최근 로그인일, 이메일 기준 정렬
                    - 페이징 지원 (기본 10개씩)
                    
                    **권한:** 관리자(ADMIN) 또는 최상위 관리자(ROOT)만 접근 가능
                    """,
            security = @SecurityRequirement(name = "cookieAuth"),
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
                                                          },
                                                          {
                                                            "memberId": 2,
                                                            "email": "admin@example.com",
                                                            "name": "관리자",
                                                            "picture": "https://example.com/admin.jpg",
                                                            "role": "ADMIN",
                                                            "status": "ACTIVE",
                                                            "lastLoginAt": "2025-09-08T11:15:00",
                                                            "createdAt": "2025-08-15T14:20:00",
                                                            "updatedAt": "2025-09-08T11:15:00"
                                                          }
                                                        ],
                                                        "totalElements": 150,
                                                        "totalPages": 15,
                                                        "size": 10,
                                                        "number": 0,
                                                        "first": true,
                                                        "last": false
                                                      },
                                                      "timestamp": "2024-01-15T14:30:00"
                                                    }
                                                    """
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "권한 없음 - 관리자 권한 필요",
                            content = @Content(
                                    schema = @Schema(implementation = CommonResponse.class),
                                    examples = @ExampleObject(
                                            name = "권한 부족",
                                            value = """
                                                    {
                                                      "success": false,
                                                      "errorCode": "ACCESS_DENIED",
                                                      "message": "관리자 권한이 필요합니다.",
                                                      "timestamp": "2024-01-15T14:30:00"
                                                    }
                                                    """
                                    )
                            )
                    )
            }
    )
    ResponseEntity<CommonResponse<Page<MemberListResponse>>> getMemberList(
            @Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "페이지 크기", example = "10")
            @RequestParam(defaultValue = "10") int size,

            @Parameter(description = "이메일 검색 (부분 일치)", example = "user@")
            @RequestParam(required = false) String email,

            @Parameter(description = "이름 검색 (부분 일치)", example = "홍길")
            @RequestParam(required = false) String name,

            @Parameter(description = "역할 필터 (USER, ADMIN, ROOT)", example = "USER")
            @RequestParam(required = false) Role role,

            @Parameter(description = "상태 필터 (ACTIVE, INACTIVE, SUSPENDED)", example = "ACTIVE")
            @RequestParam(required = false) UserStatus status,

            @Parameter(description = "정렬 기준 (createdAt, lastLoginAt, email, name)", example = "createdAt")
            @RequestParam(defaultValue = "createdAt") String sortBy,

            @Parameter(description = "정렬 방향 (asc, desc)", example = "desc")
            @RequestParam(defaultValue = "desc") String sortDirection,

            @AuthenticationPrincipal UserDetails userDetails
    );

    @Operation(
            summary = "[관리자] 회원 상세 조회",
            description = "특정 회원의 상세 정보를 조회합니다. 자소서, 이력서 작성 개수 등 통계 정보도 포함됩니다.",
            security = @SecurityRequirement(name = "cookieAuth"),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "회원 상세 조회 성공",
                            content = @Content(
                                    schema = @Schema(implementation = CommonResponse.class),
                                    examples = @ExampleObject(
                                            name = "회원 상세 조회 성공",
                                            value = """
                                                    {
                                                      "success": true,
                                                      "message": "회원 상세 조회 성공",
                                                      "data": {
                                                        "memberId": 1,
                                                        "googleId": "google_123456789",
                                                        "email": "user@example.com",
                                                        "name": "홍길동",
                                                        "picture": "https://lh3.googleusercontent.com/...",
                                                        "role": "USER",
                                                        "status": "ACTIVE",
                                                        "lastLoginAt": "2025-09-08T10:30:00",
                                                        "createdAt": "2025-09-01T09:00:00",
                                                        "updatedAt": "2025-09-08T10:30:00",
                                                        "coverLetterCount": 5,
                                                        "resumeCount": 2
                                                      },
                                                      "timestamp": "2024-01-15T14:30:00"
                                                    }
                                                    """
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "회원을 찾을 수 없음",
                            content = @Content(schema = @Schema(implementation = CommonResponse.class))
                    )
            }
    )
    ResponseEntity<CommonResponse<MemberDetailResponse>> getMemberDetail(
            @Parameter(description = "회원 ID", example = "1") @PathVariable Long memberId,
            @AuthenticationPrincipal UserDetails userDetails
    );

    @Operation(
            summary = "[관리자] 회원 상태 변경",
            description = """
                    회원의 상태를 변경합니다. 
                    
                    **변경 가능한 상태:**
                    - ACTIVE: 활성 상태 (정상 서비스 이용 가능)
                    - INACTIVE: 비활성 상태 (로그인 불가)
                    - SUSPENDED: 정지 상태 (로그인 불가)
                    
                    **제한사항:**
                    - 자기 자신의 상태는 변경할 수 없음
                    - ROOT 관리자의 상태는 ROOT 관리자만 변경 가능
                    - 상태를 비활성/정지로 변경 시 해당 사용자는 자동으로 강제 로그아웃됨
                    """,
            security = @SecurityRequirement(name = "cookieAuth"),
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "변경할 상태와 사유",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = MemberStatusUpdateRequest.class),
                            examples = @ExampleObject(
                                    name = "회원 상태 변경 요청",
                                    value = """
                                            {
                                              "status": "SUSPENDED",
                                              "reason": "부적절한 서비스 이용으로 인한 계정 정지"
                                            }
                                            """
                            )
                    )
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "회원 상태 변경 성공",
                            content = @Content(
                                    schema = @Schema(implementation = CommonResponse.class),
                                    examples = @ExampleObject(
                                            name = "상태 변경 성공",
                                            value = """
                                                    {
                                                      "success": true,
                                                      "message": "회원 상태가 변경되었습니다.",
                                                      "data": null,
                                                      "timestamp": "2024-01-15T14:30:00"
                                                    }
                                                    """
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "잘못된 요청 - 자기 자신 상태 변경 시도",
                            content = @Content(schema = @Schema(implementation = CommonResponse.class))
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "권한 없음 - ROOT 관리자 상태 변경 시도",
                            content = @Content(schema = @Schema(implementation = CommonResponse.class))
                    )
            }
    )
    ResponseEntity<CommonResponse<Void>> updateMemberStatus(
            @Parameter(description = "회원 ID", example = "1") @PathVariable Long memberId,
            @Valid @RequestBody MemberStatusUpdateRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    );

    @Operation(
            summary = "[관리자] 회원 역할 변경",
            description = """
                    회원의 역할을 변경합니다.
                    
                    **역할 종류:**
                    - USER: 일반 사용자
                    - ADMIN: 관리자
                    - ROOT: 최상위 관리자
                    
                    **제한사항:**
                    - 자기 자신의 역할은 변경할 수 없음
                    - ROOT 권한 부여는 ROOT 관리자만 가능
                    - ROOT 관리자의 역할은 ROOT 관리자만 변경 가능
                    - 동급 관리자끼리는 서로의 역할 변경 불가
                    - 역할이 축소될 경우 해당 사용자는 자동으로 강제 로그아웃됨
                    """,
            security = @SecurityRequirement(name = "cookieAuth"),
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "변경할 역할과 사유",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = MemberRoleUpdateRequest.class),
                            examples = @ExampleObject(
                                    name = "회원 역할 변경 요청",
                                    value = """
                                            {
                                              "role": "ADMIN",
                                              "reason": "관리 업무 수행을 위한 관리자 권한 부여"
                                            }
                                            """
                            )
                    )
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "회원 역할 변경 성공",
                            content = @Content(
                                    schema = @Schema(implementation = CommonResponse.class),
                                    examples = @ExampleObject(
                                            name = "역할 변경 성공",
                                            value = """
                                                    {
                                                      "success": true,
                                                      "message": "회원 역할이 변경되었습니다.",
                                                      "data": null,
                                                      "timestamp": "2024-01-15T14:30:00"
                                                    }
                                                    """
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "권한 없음",
                            content = @Content(schema = @Schema(implementation = CommonResponse.class))
                    )
            }
    )
    ResponseEntity<CommonResponse<Void>> updateMemberRole(
            @Parameter(description = "회원 ID", example = "1") @PathVariable Long memberId,
            @Valid @RequestBody MemberRoleUpdateRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    );

    @Operation(
            summary = "[관리자] 회원 통계",
            description = """
                    전체 회원 통계 정보를 조회합니다.
                    
                    **포함 정보:**
                    - 전체 회원 수, 상태별 회원 수
                    - 역할별 회원 수
                    - 오늘 가입한 회원 수
                    - 이번 달 가입한 회원 수
                    """,
            security = @SecurityRequirement(name = "cookieAuth"),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "회원 통계 조회 성공",
                            content = @Content(
                                    schema = @Schema(implementation = CommonResponse.class),
                                    examples = @ExampleObject(
                                            name = "회원 통계",
                                            value = """
                                                    {
                                                      "success": true,
                                                      "message": "회원 통계 조회 성공",
                                                      "data": {
                                                        "totalMembers": 1250,
                                                        "activeMembers": 1180,
                                                        "inactiveMembers": 65,
                                                        "suspendedMembers": 5,
                                                        "userRoleCount": 1200,
                                                        "adminRoleCount": 48,
                                                        "rootRoleCount": 2,
                                                        "todayNewMembers": 12,
                                                        "monthlyNewMembers": 89
                                                      },
                                                      "timestamp": "2024-01-15T14:30:00"
                                                    }
                                                    """
                                    )
                            )
                    )
            }
    )
    ResponseEntity<CommonResponse<Object>> getMemberStatistics(
            @AuthenticationPrincipal UserDetails userDetails
    );

    @Operation(
            summary = "[관리자] 회원 강제 로그아웃",
            description = """
                    특정 회원을 강제로 로그아웃시킵니다. 
                    
                    **처리 내용:**
                    - 해당 사용자의 모든 세션 무효화
                    - Access Token 및 Refresh Token 블랙리스트 등록
                    - 쿠키에서 인증 정보 삭제
                    
                    **제한사항:**
                    - 자기 자신을 강제 로그아웃할 수 없음
                    - ROOT 관리자는 ROOT 관리자만 강제 로그아웃 가능
                    """,
            security = @SecurityRequirement(name = "cookieAuth"),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "강제 로그아웃 성공",
                            content = @Content(
                                    schema = @Schema(implementation = CommonResponse.class),
                                    examples = @ExampleObject(
                                            name = "강제 로그아웃 성공",
                                            value = """
                                                    {
                                                      "success": true,
                                                      "message": "회원이 강제 로그아웃되었습니다.",
                                                      "data": null,
                                                      "timestamp": "2024-01-15T14:30:00"
                                                    }
                                                    """
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "자기 자신 강제 로그아웃 시도",
                            content = @Content(schema = @Schema(implementation = CommonResponse.class))
                    )
            }
    )
    ResponseEntity<CommonResponse<Void>> forceMemberLogout(
            @Parameter(description = "강제 로그아웃할 회원 ID", example = "1") @PathVariable Long memberId,
            @AuthenticationPrincipal UserDetails userDetails
    );
}