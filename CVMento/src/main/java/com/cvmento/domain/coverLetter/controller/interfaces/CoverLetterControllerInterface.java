package com.cvmento.domain.coverLetter.controller.interfaces;

import com.cvmento.domain.coverLetter.dto.request.CoverLetterSaveRequest;
import com.cvmento.domain.coverLetter.dto.request.CoverLetterUpdateRequest;
import com.cvmento.domain.coverLetter.dto.response.CoverLetterDetailResponse;
import com.cvmento.domain.coverLetter.dto.response.CoverLetterListResponse;
import com.cvmento.domain.coverLetter.dto.response.CoverLetterStatusListResponse;
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

@Tag(name = "자소서 관리", description = "자소서 CRUD API - 저장, 수정, 삭제, 조회 기능 제공")
public interface CoverLetterControllerInterface {

    @Operation(
            summary = "자소서 저장",
            description = "원본 자소서 또는 AI 첨삭된 자소서를 저장합니다. 지원분야와 경력 정보를 포함합니다.",
            security = @SecurityRequirement(name = "cookieAuth"),
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "저장할 자소서 정보 (지원분야, 경력정보 포함)",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = CoverLetterSaveRequest.class),
                            examples = {
                                    @ExampleObject(
                                            name = "원본 자소서 저장",
                                            description = "사용자가 직접 작성한 원본 자소서",
                                            value = """
                                                    {
                                                      "title": "네이버 백엔드 개발자 지원",
                                                      "content": "저는 소프트웨어 개발에 대한 열정을 바탕으로 다양한 프로젝트를 수행해왔습니다. 특히 백엔드 개발에 관심이 많아 Spring Boot와 JPA를 활용한 REST API 개발 경험이 있습니다.",
                                                      "jobField": "백엔드 개발자",
                                                      "experienceYears": 1,
                                                      "isAiImproved": false
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "AI 첨삭 자소서 저장",
                                            description = "AI가 첨삭한 자소서",
                                            value = """
                                                    {
                                                      "title": "네이버 백엔드 개발자 지원",
                                                      "content": "저는 소프트웨어 개발 분야에서 지속적인 성장을 추구하는 주니어 개발자로서, 1년간의 실무 경험을 통해 견고한 기술적 기반을 구축해왔습니다. Spring Boot와 JPA를 활용한 REST API 개발 프로젝트에서 성능 최적화와 코드 품질 향상에 기여했습니다.",
                                                      "jobField": "백엔드 개발자",
                                                      "experienceYears": 1,
                                                      "isAiImproved": true
                                                    }
                                                    """
                                    )
                            }
                    )
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "자소서 저장 성공",
                            content = @Content(
                                    schema = @Schema(implementation = CommonResponse.class),
                                    examples = @ExampleObject(
                                            name = "자소서 저장 성공 응답",
                                            value = """
                                                    {
                                                      "success": true,
                                                      "message": "원본 자소서가 저장되었습니다.",
                                                      "data": null,
                                                      "timestamp": "2024-01-15T14:30:00"
                                                    }
                                                    """
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "잘못된 요청 데이터",
                            content = @Content(
                                    schema = @Schema(implementation = CommonResponse.class),
                                    examples = @ExampleObject(
                                            name = "유효성 검사 실패",
                                            value = """
                                                    {
                                                      "timestamp": "2024-01-15T14:30:00",
                                                      "status": 400,
                                                      "error": "Bad Request",
                                                      "errorCode": "VALIDATION_ERROR",
                                                      "message": "입력값이 올바르지 않습니다.",
                                                      "errors": {
                                                        "content": "내용은 100자 이상 2000자 이하로 작성해주세요.",
                                                        "title": "제목은 필수입니다."
                                                      }
                                                    }
                                                    """
                                    )
                            )
                    )
            }
    )
    ResponseEntity<CommonResponse<Void>> saveCoverLetter(
            @Valid @RequestBody CoverLetterSaveRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    );

    @Operation(
            summary = "자소서 수정",
            description = "기존 자소서를 수정합니다. 제목에 [수정본] 접두사가 자동으로 추가됩니다.",
            security = @SecurityRequirement(name = "cookieAuth"),
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "수정할 자소서 정보",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = CoverLetterUpdateRequest.class),
                            examples = @ExampleObject(
                                    name = "자소서 수정 요청",
                                    value = """
                                            {
                                              "title": "네이버 백엔드 개발자 지원",
                                              "content": "저는 소프트웨어 개발에 대한 깊은 열정을 바탕으로 지속적으로 성장해온 개발자입니다. 특히 백엔드 개발 분야에서 Spring Boot와 JPA를 활용한 다양한 프로젝트를 통해 실무 경험을 쌓아왔습니다. 최근에는 성능 최적화와 코드 품질 향상에 특별한 관심을 가지고 있습니다.",
                                              "jobField": "백엔드 개발자",
                                              "experienceYears": 2
                                            }
                                            """
                            )
                    )
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "자소서 수정 성공",
                            content = @Content(
                                    schema = @Schema(implementation = CommonResponse.class),
                                    examples = @ExampleObject(
                                            name = "자소서 수정 성공 응답",
                                            value = """
                                                    {
                                                      "success": true,
                                                      "message": "자소서가 수정되었습니다.",
                                                      "data": null,
                                                      "timestamp": "2024-01-15T14:30:00"
                                                    }
                                                    """
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "자소서를 찾을 수 없음",
                            content = @Content(schema = @Schema(implementation = CommonResponse.class))
                    )
            }
    )
    ResponseEntity<CommonResponse<Void>> updateCoverLetter(
            @Parameter(description = "수정할 자소서 ID") @PathVariable Long coverLetterId,
            @Valid @RequestBody CoverLetterUpdateRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    );

    @Operation(
            summary = "자소서 삭제 (소프트 삭제)",
            description = "자소서를 삭제합니다. 실제로는 상태만 변경되어 복구가 가능합니다.",
            security = @SecurityRequirement(name = "cookieAuth"),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "자소서 삭제 성공",
                            content = @Content(
                                    schema = @Schema(implementation = CommonResponse.class),
                                    examples = @ExampleObject(
                                            name = "자소서 삭제 성공 응답",
                                            value = """
                                                    {
                                                      "success": true,
                                                      "message": "자소서가 삭제되었습니다.",
                                                      "data": null,
                                                      "timestamp": "2024-01-15T14:30:00"
                                                    }
                                                    """
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "자소서를 찾을 수 없음",
                            content = @Content(schema = @Schema(implementation = CommonResponse.class))
                    )
            }
    )
    ResponseEntity<CommonResponse<Void>> deleteCoverLetter(
            @Parameter(description = "삭제할 자소서 ID") @PathVariable Long coverLetterId,
            @AuthenticationPrincipal UserDetails userDetails
    );

    @Operation(
            summary = "자소서 목록 조회",
            description = "사용자의 자소서 목록을 페이징으로 조회합니다. 최신 수정일 순으로 정렬됩니다.",
            security = @SecurityRequirement(name = "cookieAuth"),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "자소서 목록 조회 성공",
                            content = @Content(
                                    schema = @Schema(implementation = CommonResponse.class),
                                    examples = @ExampleObject(
                                            name = "자소서 목록 조회 응답",
                                            value = """
                                                    {
                                                      "success": true,
                                                      "message": "자소서 목록 조회 성공",
                                                      "data": {
                                                        "content": [
                                                          {
                                                            "coverLetterId": 1,
                                                            "title": "[AI첨삭] 네이버 백엔드 개발자 지원",
                                                            "content": "저는 백엔드 개발 분야에서 3년간의 실무 경험을 통해 확고한 기술적 역량을 구축해왔습니다...",
                                                            "jobField": "백엔드 개발자",
                                                            "experience": "3년",
                                                            "createdAt": "2025-09-01T10:30:00",
                                                            "updatedAt": "2025-09-01T10:35:00"
                                                          }
                                                        ],
                                                        "pageable": {
                                                          "pageNumber": 0,
                                                          "pageSize": 5
                                                        },
                                                        "totalElements": 15,
                                                        "totalPages": 3
                                                      },
                                                      "timestamp": "2024-01-15T14:30:00"
                                                    }
                                                    """
                                    )
                            )
                    )
            }
    )
    ResponseEntity<CommonResponse<Page<CoverLetterListResponse>>> getCoverLetters(
            @Parameter(description = "페이지 번호 (0부터 시작)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기") @RequestParam(defaultValue = "5") int size,
            @Parameter(description = "뷰 타입 (thumbnail: 미리보기, 그외: 전체내용)") @RequestParam(required = false) String view,
            @AuthenticationPrincipal UserDetails userDetails
    );

    @Operation(
            summary = "자소서 상세 조회",
            description = "특정 자소서의 상세 정보를 조회합니다.",
            security = @SecurityRequirement(name = "cookieAuth"),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "자소서 조회 성공",
                            content = @Content(
                                    schema = @Schema(implementation = CommonResponse.class),
                                    examples = @ExampleObject(
                                            name = "자소서 상세 조회 응답",
                                            value = """
                                                    {
                                                      "success": true,
                                                      "message": "자소서 조회 성공",
                                                      "data": {
                                                        "coverLetterId": 1,
                                                        "title": "[원본] 네이버 백엔드 개발자 지원",
                                                        "content": "저는 소프트웨어 개발에 대한 열정을 바탕으로 다양한 프로젝트를 수행해왔습니다. 특히 백엔드 개발에 관심이 많아 Spring Boot와 JPA를 활용한 REST API 개발 경험이 있습니다. 대학교 재학 중 진행한 팀 프로젝트에서는 주도적으로 서버 아키텍처를 설계하고 구현했으며, 이를 통해 협업과 문제해결 능력을 기를 수 있었습니다.",
                                                        "jobField": "백엔드 개발자",
                                                        "experience": "1년",
                                                        "createdAt": "2025-09-01T10:30:00",
                                                        "updatedAt": "2025-09-01T10:30:00"
                                                      },
                                                      "timestamp": "2024-01-15T14:30:00"
                                                    }
                                                    """
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "자소서를 찾을 수 없음",
                            content = @Content(schema = @Schema(implementation = CommonResponse.class))
                    )
            }
    )
    ResponseEntity<CommonResponse<CoverLetterDetailResponse>> getCoverLetter(
            @Parameter(description = "자소서 ID") @PathVariable Long coverLetterId,
            @AuthenticationPrincipal UserDetails userDetails
    );

    @Operation(
            summary = "자소서 복구 (관리자 전용)",
            description = "삭제된 자소서 ID를 받아서 복구합니다. 관리자만 사용 가능한 기능입니다.",
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
                    @ApiResponse(
                            responseCode = "403",
                            description = "관리자 권한 필요",
                            content = @Content(schema = @Schema(implementation = CommonResponse.class))
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "복구할 수 있는 자소서를 찾을 수 없음",
                            content = @Content(schema = @Schema(implementation = CommonResponse.class))
                    )
            }
    )
    ResponseEntity<CommonResponse<Void>> restoreCoverLetter(
            @Parameter(description = "복구할 자소서 ID") @PathVariable Long coverLetterId,
            @AuthenticationPrincipal UserDetails userDetails
    );

    /**
     * 자소서 소프트 삭제 리스트 조회 (관리자 전용)
     */
    @Operation(
            summary = "삭제된 자소서 목록 조회 (관리자 전용)",
            description = "소프트 삭제된 자소서들을 페이징과 필터링으로 조회합니다. 삭제 예정일 임박순으로 정렬됩니다.",
            security = @SecurityRequirement(name = "cookieAuth"),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "삭제된 자소서 목록 조회 성공",
                            content = @Content(
                                    schema = @Schema(implementation = CommonResponse.class),
                                    examples = @ExampleObject(
                                            name = "삭제된 자소서 목록 조회 응답",
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
                                                        "createdAt": "2025-09-01T10:30:00",
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
                    @ApiResponse(
                            responseCode = "400",
                            description = "잘못된 요청 데이터",
                            content = @Content(
                                    schema = @Schema(implementation = CommonResponse.class),
                                    examples = {
                                            @ExampleObject(
                                                    name = "상태값 검증 실패",
                                                    value = """
                                        {
                                          "timestamp": "2025-09-16T14:30:00",
                                          "status": 400,
                                          "error": "Bad Request",
                                          "errorCode": "INVALID_STATUS",
                                          "message": "올바르지 않은 상태값입니다.",
                                          "errors": {}
                                        }
                                        """
                                            ),
                                            @ExampleObject(
                                                    name = "필드 검증 실패",
                                                    value = """
                                        {
                                          "timestamp": "2025-09-16T14:30:00",
                                          "status": 400,
                                          "error": "Bad Request",
                                          "errorCode": "VALIDATION_ERROR",
                                          "message": "입력값이 올바르지 않습니다.",
                                          "errors": {
                                            "email": "이메일은 320자를 초과할 수 없습니다.",
                                            "title": "제목은 100자를 초과할 수 없습니다."
                                          }
                                        }
                                        """
                                            )
                                    }
                            )
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "관리자 권한 필요",
                            content = @Content(
                                    schema = @Schema(implementation = CommonResponse.class),
                                    examples = @ExampleObject(
                                            name = "권한 없음 응답",
                                            value = """
                                                {
                                                  "success": false,
                                                  "message": "관리자 권한이 필요합니다.",
                                                  "errorCode": "ACCESS_DENIED",
                                                  "timestamp": "2025-09-16T14:30:00"
                                                }
                                                """
                                    )
                            )
                    )
            }
    )
    ResponseEntity<CommonResponse<Page<CoverLetterStatusListResponse>>> getDeletedCoverLetters(
            @Parameter(description = "페이지 번호 (0부터 시작)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기 (기본값: 20)") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "자소서 상태 (기본값: DELETED)") @RequestParam(defaultValue = "DELETED") String status,
            @Parameter(description = "작성자 이메일 필터링 (부분 검색, 최대 320자)") @RequestParam(required = false) @Size(max = 320, message = "이메일은 320자를 초과할 수 없습니다.") String email,
            @Parameter(description = "글 제목 필터링 (부분 검색, 최대 100자)") @RequestParam(required = false) @Size(max = 100, message = "제목은 100자를 초과할 수 없습니다.") String title,
            @AuthenticationPrincipal UserDetails userDetails
    );
}