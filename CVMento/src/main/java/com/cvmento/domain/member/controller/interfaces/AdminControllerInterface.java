package com.cvmento.domain.member.controller.interfaces;

import com.cvmento.domain.coverLetter.dto.response.CoverLetterStatusListResponse;
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
import jakarta.validation.constraints.Size;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PathVariable;
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
}