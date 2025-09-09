package com.cvmento.domain.coverLetter.controller;

import com.cvmento.domain.coverLetter.dto.request.CoverLetterSaveRequest;
import com.cvmento.domain.coverLetter.dto.request.CoverLetterUpdateRequest;
import com.cvmento.domain.coverLetter.dto.response.CoverLetterDetailResponse;
import com.cvmento.domain.coverLetter.dto.response.CoverLetterListResponse;
import com.cvmento.domain.coverLetter.service.CoverLetterService;
import com.cvmento.global.common.dto.CommonResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@Tag(name = "자소서 관리", description = "자소서 CRUD API")
@RestController
@RequestMapping("/api/v1/cover-letters")
@RequiredArgsConstructor
public class CoverLetterController {

    private final CoverLetterService coverLetterService;

    @Operation(
            summary = "자소서 저장",
            description = "원본 자소서 또는 AI 첨삭된 자소서를 저장합니다. 지원분야와 경력 정보를 포함합니다.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "저장할 자소서 정보 (지원분야, 경력정보 포함)",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = CoverLetterSaveRequest.class),
                            examples = {
                                    @ExampleObject(
                                            name = "원본 자소서 저장",
                                            description = "사용자가 직접 작성한 원본 자소서",
                                            value = "{\n" +
                                                    "  \"title\": \"네이버 백엔드 개발자 지원\",\n" +
                                                    "  \"content\": \"저는 소프트웨어 개발에 대한 열정을 바탕으로 다양한 프로젝트를 수행해왔습니다. 특히 백엔드 개발에 관심이 많아 Spring Boot와 JPA를 활용한 REST API 개발 경험이 있습니다.\",\n" +
                                                    "  \"jobField\": \"백엔드 개발자\",\n" +
                                                    "  \"experienceYears\": 1,\n" +
                                                    "  \"isAiImproved\": false\n" +
                                                    "}"
                                    ),
                                    @ExampleObject(
                                            name = "AI 첨삭 자소서 저장",
                                            description = "AI가 첨삭한 자소서",
                                            value = "{\n" +
                                                    "  \"title\": \"네이버 백엔드 개발자 지원\",\n" +
                                                    "  \"content\": \"저는 소프트웨어 개발 분야에서 지속적인 성장을 추구하는 주니어 개발자로서, 1년간의 실무 경험을 통해 견고한 기술적 기반을 구축해왔습니다. Spring Boot와 JPA를 활용한 REST API 개발 프로젝트에서 성능 최적화와 코드 품질 향상에 기여했습니다.\",\n" +
                                                    "  \"jobField\": \"백엔드 개발자\",\n" +
                                                    "  \"experienceYears\": 1,\n" +
                                                    "  \"isAiImproved\": true\n" +
                                                    "}"
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
                                            value = "{\n" +
                                                    "  \"success\": true,\n" +
                                                    "  \"message\": \"원본 자소서가 저장되었습니다.\",\n" +
                                                    "  \"data\": null\n" +
                                                    "}"
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "잘못된 요청 데이터",
                            content = @Content(
                                    schema = @Schema(implementation = CommonResponse.class)
                            )
                    )
            }
    )
    @PostMapping
    public ResponseEntity<CommonResponse<Void>> saveCoverLetter(
            @Valid @RequestBody CoverLetterSaveRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        String userEmail = userDetails.getUsername();
        coverLetterService.saveCoverLetter(request, userEmail);

        String message = request.isAiImproved() ?
                "AI 첨삭된 자소서가 저장되었습니다." :
                "원본 자소서가 저장되었습니다.";

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(CommonResponse.success(message, null));
    }

    @Operation(
            summary = "자소서 목록 조회",
            description = "사용자의 자소서 목록을 페이징으로 조회합니다. 최신 수정일 순으로 정렬됩니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "자소서 목록 조회 성공",
                            content = @Content(
                                    schema = @Schema(implementation = CommonResponse.class),
                                    examples = @ExampleObject(
                                            name = "자소서 목록 조회 응답",
                                            value = "{\n" +
                                                    "  \"success\": true,\n" +
                                                    "  \"message\": \"자소서 목록 조회 성공\",\n" +
                                                    "  \"data\": {\n" +
                                                    "    \"content\": [\n" +
                                                    "      {\n" +
                                                    "        \"coverLetterId\": 1,\n" +
                                                    "        \"title\": \"[AI첨삭] 네이버 백엔드 개발자 지원\",\n" +
                                                    "        \"content\": \"저는 백엔드 개발 분야에서 3년간의 실무 경험을 통해 확고한 기술적 역량을 구축해왔습니다...\",\n" +
                                                    "        \"jobField\": \"백엔드 개발자\",\n" +
                                                    "        \"experience\": \"3년\",\n" +
                                                    "        \"createdAt\": \"2025-09-01T10:30:00\",\n" +
                                                    "        \"updatedAt\": \"2025-09-01T10:35:00\"\n" +
                                                    "      }\n" +
                                                    "    ],\n" +
                                                    "    \"pageable\": {\n" +
                                                    "      \"pageNumber\": 0,\n" +
                                                    "      \"pageSize\": 5\n" +
                                                    "    },\n" +
                                                    "    \"totalElements\": 15,\n" +
                                                    "    \"totalPages\": 3\n" +
                                                    "  }\n" +
                                                    "}"
                                    )
                            )
                    )
            }
    )
    @GetMapping
    public ResponseEntity<CommonResponse<Page<CoverLetterListResponse>>> getCoverLetters(
            @Parameter(description = "페이지 번호 (0부터 시작)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기") @RequestParam(defaultValue = "5") int size,
            @Parameter(description = "뷰 타입 (thumbnail: 미리보기, 그외: 전체내용)") @RequestParam(required = false) String view,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        String userEmail = userDetails.getUsername();
        Pageable pageable = PageRequest.of(page, size);
        Page<CoverLetterListResponse> response = coverLetterService.getCoverLetters(userEmail, pageable, view);

        return ResponseEntity.ok(CommonResponse.success("자소서 목록 조회 성공", response));
    }

    @Operation(
            summary = "자소서 상세 조회",
            description = "특정 자소서의 상세 정보를 조회합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "자소서 조회 성공",
                            content = @Content(
                                    schema = @Schema(implementation = CommonResponse.class),
                                    examples = @ExampleObject(
                                            name = "자소서 상세 조회 응답",
                                            value = "{\n" +
                                                    "  \"success\": true,\n" +
                                                    "  \"message\": \"자소서 조회 성공\",\n" +
                                                    "  \"data\": {\n" +
                                                    "    \"coverLetterId\": 1,\n" +
                                                    "    \"title\": \"[원본] 네이버 백엔드 개발자 지원\",\n" +
                                                    "    \"content\": \"저는 소프트웨어 개발에 대한 열정을 바탕으로 다양한 프로젝트를 수행해왔습니다. 특히 백엔드 개발에 관심이 많아 Spring Boot와 JPA를 활용한 REST API 개발 경험이 있습니다. 대학교 재학 중 진행한 팀 프로젝트에서는 주도적으로 서버 아키텍처를 설계하고 구현했으며, 이를 통해 협업과 문제해결 능력을 기를 수 있었습니다.\",\n" +
                                                    "    \"jobField\": \"백엔드 개발자\",\n" +
                                                    "    \"experience\": \"1년\",\n" +
                                                    "    \"createdAt\": \"2025-09-01T10:30:00\",\n" +
                                                    "    \"updatedAt\": \"2025-09-01T10:30:00\"\n" +
                                                    "  }\n" +
                                                    "}"
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "자소서를 찾을 수 없음",
                            content = @Content(
                                    schema = @Schema(implementation = CommonResponse.class)
                            )
                    )
            }
    )
    @GetMapping("/{coverLetterId}")
    public ResponseEntity<CommonResponse<CoverLetterDetailResponse>> getCoverLetter(
            @Parameter(description = "자소서 ID") @PathVariable Long coverLetterId,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        String userEmail = userDetails.getUsername();
        CoverLetterDetailResponse response = coverLetterService.getCoverLetter(coverLetterId, userEmail);

        return ResponseEntity.ok(CommonResponse.success("자소서 조회 성공", response));
    }

    @Operation(
            summary = "자소서 수정",
            description = "기존 자소서를 수정합니다. 제목에 [수정본] 접두사가 자동으로 추가됩니다.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "수정할 자소서 정보",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = CoverLetterUpdateRequest.class),
                            examples = @ExampleObject(
                                    name = "자소서 수정 요청",
                                    value = "{\n" +
                                            "  \"title\": \"네이버 백엔드 개발자 지원\",\n" +
                                            "  \"content\": \"저는 소프트웨어 개발에 대한 깊은 열정을 바탕으로 지속적으로 성장해온 개발자입니다. 특히 백엔드 개발 분야에서 Spring Boot와 JPA를 활용한 다양한 프로젝트를 통해 실무 경험을 쌓아왔습니다. 최근에는 성능 최적화와 코드 품질 향상에 특별한 관심을 가지고 있습니다.\",\n" +
                                            "  \"jobField\": \"백엔드 개발자\",\n" +
                                            "  \"experienceYears\": 2\n" +
                                            "}"
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
                                            value = "{\n" +
                                                    "  \"success\": true,\n" +
                                                    "  \"message\": \"자소서가 수정되었습니다.\",\n" +
                                                    "  \"data\": null\n" +
                                                    "}"
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "자소서를 찾을 수 없음",
                            content = @Content(
                                    schema = @Schema(implementation = CommonResponse.class)
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "잘못된 요청 데이터",
                            content = @Content(
                                    schema = @Schema(implementation = CommonResponse.class)
                            )
                    )
            }
    )
    @PutMapping("/{coverLetterId}")
    public ResponseEntity<CommonResponse<Void>> updateCoverLetter(
            @Parameter(description = "수정할 자소서 ID") @PathVariable Long coverLetterId,
            @Valid @RequestBody CoverLetterUpdateRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        String userEmail = userDetails.getUsername();
        coverLetterService.updateCoverLetter(coverLetterId, request, userEmail);

        return ResponseEntity.ok(CommonResponse.success("자소서가 수정되었습니다.", null));
    }


    /**
     * 자소서 삭제 API (소프트삭제) - 추후 구현 예정
     */
}