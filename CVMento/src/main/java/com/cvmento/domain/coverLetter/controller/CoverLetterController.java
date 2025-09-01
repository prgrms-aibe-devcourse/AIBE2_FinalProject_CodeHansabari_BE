package com.cvmento.domain.coverLetter.controller;

import com.cvmento.domain.coverLetter.dto.request.CoverLetterSaveRequest;
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
            description = "원본 자소서 또는 AI 첨삭된 자소서를 저장합니다.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "저장할 자소서 정보",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = CoverLetterSaveRequest.class),
                            examples = {
                                    @ExampleObject(
                                            name = "원본 자소서 저장",
                                            description = "사용자가 직접 작성한 원본 자소서",
                                            value = "{\n" +
                                                    "  \"title\": \"네이버 백엔드 개발자 지원\",\n" +
                                                    "  \"content\": \"팀 프로젝트와 인턴십에서 백엔드 아키텍처 설계와 API 개발을 주도하며 성능과 안정성을 개선했습니다. 새로운 스택을 빠르게 학습해 배포 자동화와 코드 리뷰 문화를 정착시켰고, 사용자 경험을 높이는 문제 해결에 즐거움을 느낍니다. 네이버의 대규모 트래픽 환경에서 성장하고 기여하고자 지원합니다.\",\n" +
                                                    "  \"isAiImproved\": false\n" +
                                                    "}"
                                    ),
                                    @ExampleObject(
                                            name = "AI 첨삭 자소서 저장",
                                            description = "AI가 첨삭한 자소서",
                                            value = "{\n" +
                                                    "  \"title\": \"네이버 백엔드 개발자 지원\",\n" +
                                                    "  \"content\": \"풀스택 관점에서 데이터 모델링, REST 설계, 테스트 자동화까지 일관된 기준으로 개선했습니다. 리뷰 체크리스트와 CI 파이프라인을 도입해 릴리즈 시간을 단축했고, 장애 복구 절차를 문서화했습니다. 실사용자 피드백을 바탕으로 기능을 반복 개선하며 팀의 목표 달성에 기여했습니다.\",\n" +
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
                                    schema = @Schema(implementation = CommonResponse.class)
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
                                    schema = @Schema(implementation = CommonResponse.class)
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
}