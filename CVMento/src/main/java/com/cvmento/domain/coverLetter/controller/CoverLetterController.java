package com.cvmento.domain.coverLetter.controller;

import com.cvmento.domain.coverLetter.dto.request.CoverLetterSaveRequest;
import com.cvmento.domain.coverLetter.service.CoverLetterService;
import com.cvmento.global.common.dto.CommonResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@Tag(name = "자소서 관리", description = "자소서 저장 API")
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
                                                    "  \"content\": \"저는 소프트웨어 개발에 대한 열정을 바탕으로...\",\n" +
                                                    "  \"isAiImproved\": false\n" +
                                                    "}"
                                    ),
                                    @ExampleObject(
                                            name = "AI 첨삭 자소서 저장",
                                            description = "AI가 첨삭한 자소서",
                                            value = "{\n" +
                                                    "  \"title\": \"네이버 백엔드 개발자 지원\",\n" +
                                                    "  \"content\": \"저는 소프트웨어 개발 분야에서 지속적인 성장을 추구하는 개발자로서...\",\n" +
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
}