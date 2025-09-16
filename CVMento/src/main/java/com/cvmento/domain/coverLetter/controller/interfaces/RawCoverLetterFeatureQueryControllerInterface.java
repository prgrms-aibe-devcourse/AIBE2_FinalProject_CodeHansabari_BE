package com.cvmento.domain.coverLetter.controller.interfaces;

import com.cvmento.domain.coverLetter.dto.response.RawCoverLetterFeaturePageResponse;
import com.cvmento.domain.coverLetter.enums.FeaturesCategory;
import com.cvmento.global.common.dto.CommonResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "합격 자소서 Raw 특징 조회", description = "원본(raw) 특징 조회 API - 페이징, 카테고리별 조회")
public interface RawCoverLetterFeatureQueryControllerInterface {

    @Operation(
            summary = "Raw 특징 페이징 조회",
            description = "원본(raw) 특징을 생성일 기준 내림차순으로 페이징 조회합니다.",
            security = @SecurityRequirement(name = "cookieAuth"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = CommonResponse.class))),
                    @ApiResponse(responseCode = "403", description = "권한 없음", content = @Content(schema = @Schema(implementation = CommonResponse.class)))
            }
    )
    ResponseEntity<CommonResponse<RawCoverLetterFeaturePageResponse>> getRawFeaturesPaged(
            @Parameter(description = "페이지 번호 (0부터)", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기 (최대 100)", example = "20") @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal UserDetails userDetails
    );

    @Operation(
            summary = "카테고리별 Raw 특징 페이징 조회",
            description = "특정 카테고리(EXPRESSION, STRUCTURE, CONTENT)의 원본(raw) 특징을 페이징 조회합니다.",
            security = @SecurityRequirement(name = "cookieAuth"),
            responses = {
                    @ApiResponse(responseCode = "200", description = "조회 성공", content = @Content(schema = @Schema(implementation = CommonResponse.class))),
                    @ApiResponse(responseCode = "403", description = "권한 없음", content = @Content(schema = @Schema(implementation = CommonResponse.class)))
            }
    )
    ResponseEntity<CommonResponse<RawCoverLetterFeaturePageResponse>> getRawFeaturesByCategoryPaged(
            @Parameter(description = "특징 카테고리", example = "EXPRESSION") @PathVariable FeaturesCategory category,
            @Parameter(description = "페이지 번호 (0부터)", example = "0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기 (최대 100)", example = "20") @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal UserDetails userDetails
    );
}


