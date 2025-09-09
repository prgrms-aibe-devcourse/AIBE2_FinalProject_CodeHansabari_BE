package com.cvmento.domain.resume.controller;

import com.cvmento.domain.resume.dto.request.ResumeSaveRequest;
import com.cvmento.domain.resume.service.ResumeService;
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

@Tag(name = "이력서 관리", description = "이력서 CRUD API")
@RestController
@RequestMapping("/api/v1/resumes")
@RequiredArgsConstructor
public class ResumeController {

    private final ResumeService resumeService;

    /**
     * 이력서 저장
     */
    @Operation(
            summary = "이력서 저장",
            description = "새로운 이력서를 저장합니다. 이력서 제목과 기본 정보, 학력, 경력, 프로젝트 등 모든 정보를 포함합니다.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "저장할 이력서 전체 정보",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = ResumeSaveRequest.class),
                            examples = {
                                    @ExampleObject(
                                            name = "신입 개발자 이력서",
                                            description = "신입 개발자의 기본 이력서 예시",
                                            value = "{\n" +
                                                    "  \"title\": \"네이버 백엔드 개발자 지원용 이력서\",\n" +
                                                    "  \"type\": \"DEFAULT\",\n" +
                                                    "  \"name\": \"김개발\",\n" +
                                                    "  \"email\": \"kim@example.com\",\n" +
                                                    "  \"birthYear\": 1995,\n" +
                                                    "  \"phone\": \"010-1234-5678\",\n" +
                                                    "  \"careerType\": \"FRESHMAN\",\n" +
                                                    "  \"fieldName\": \"백엔드 개발자\",\n" +
                                                    "  \"introduction\": \"열정적인 신입 개발자입니다.\",\n" +
                                                    "  \"githubUrl\": \"https://github.com/kimdev\",\n" +
                                                    "  \"blogUrl\": null,\n" +
                                                    "  \"notionUrl\": null,\n" +
                                                    "  \"educations\": [\n" +
                                                    "    {\n" +
                                                    "      \"schoolName\": \"서울대학교\",\n" +
                                                    "      \"major\": \"컴퓨터공학과\",\n" +
                                                    "      \"degreeLevel\": \"BACHELOR\",\n" +
                                                    "      \"personalGpa\": 3.8,\n" +
                                                    "      \"totalGpa\": 4.5,\n" +
                                                    "      \"graduationDate\": \"2024-02-15\"\n" +
                                                    "    }\n" +
                                                    "  ],\n" +
                                                    "  \"techStacks\": [\n" +
                                                    "    {\n" +
                                                    "      \"techStackId\": 1,\n" +
                                                    "      \"proficiencyLevel\": \"INTERMEDIATE\"\n" +
                                                    "    }\n" +
                                                    "  ],\n" +
                                                    "  \"customLinks\": [],\n" +
                                                    "  \"careers\": [],\n" +
                                                    "  \"projects\": [],\n" +
                                                    "  \"trainings\": [],\n" +
                                                    "  \"additionalInfos\": []\n" +
                                                    "}"
                                    ),
                                    @ExampleObject(
                                            name = "경력 개발자 이력서",
                                            description = "경력 개발자의 상세 이력서 예시",
                                            value = "{\n" +
                                                    "  \"title\": \"카카오 시니어 개발자 지원용 이력서\",\n" +
                                                    "  \"type\": \"MODERN\",\n" +
                                                    "  \"name\": \"박경력\",\n" +
                                                    "  \"email\": \"park@example.com\",\n" +
                                                    "  \"birthYear\": 1990,\n" +
                                                    "  \"phone\": \"010-9876-5432\",\n" +
                                                    "  \"careerType\": \"EXPERIENCED\",\n" +
                                                    "  \"fieldName\": \"풀스택 개발자\",\n" +
                                                    "  \"introduction\": \"5년 경력의 풀스택 개발자입니다.\",\n" +
                                                    "  \"githubUrl\": \"https://github.com/parkdev\",\n" +
                                                    "  \"blogUrl\": \"https://parkdev.blog\",\n" +
                                                    "  \"notionUrl\": \"https://notion.so/parkdev\",\n" +
                                                    "  \"educations\": [],\n" +
                                                    "  \"techStacks\": [],\n" +
                                                    "  \"customLinks\": [],\n" +
                                                    "  \"careers\": [],\n" +
                                                    "  \"projects\": [],\n" +
                                                    "  \"trainings\": [],\n" +
                                                    "  \"additionalInfos\": []\n" +
                                                    "}"
                                    )
                            }
                    )
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "이력서 저장 성공",
                            content = @Content(
                                    schema = @Schema(implementation = CommonResponse.class),
                                    examples = @ExampleObject(
                                            name = "이력서 저장 성공 응답",
                                            value = "{\n" +
                                                    "  \"success\": true,\n" +
                                                    "  \"message\": \"이력서가 성공적으로 저장되었습니다.\",\n" +
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
    public ResponseEntity<CommonResponse<Void>> saveResume(
            @Valid @RequestBody ResumeSaveRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        String userEmail = userDetails.getUsername();
        resumeService.saveResume(request, userEmail);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(CommonResponse.success("이력서가 성공적으로 저장되었습니다.", null));
    }

    /**
     * 이력서 수정
     */
}