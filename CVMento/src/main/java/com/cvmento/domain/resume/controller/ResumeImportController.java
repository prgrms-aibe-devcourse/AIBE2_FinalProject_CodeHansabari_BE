package com.cvmento.domain.resume.controller;

import com.cvmento.domain.resume.dto.response.ResumeImportResponse;
import com.cvmento.domain.resume.service.ResumeImportService;
import com.cvmento.global.common.dto.CommonResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "이력서 변환", description = "이력서 파일을 업로드하여 CVMento 형식으로 변환")
@RestController
@RequestMapping("/api/v1/resume-import")
@RequiredArgsConstructor
@Slf4j
public class ResumeImportController {

    private final ResumeImportService resumeImportService;

    @Operation(
            summary = "이력서 파일 변환",
            description = "PDF 또는 이미지 형태의 이력서를 업로드하여 CVMento 이력서 형식으로 변환하고 자동으로 저장합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "이력서 변환 성공",
                            content = @Content(
                                    schema = @Schema(implementation = CommonResponse.class),
                                    examples = @ExampleObject(
                                            name = "변환 성공 응답",
                                            value = """
                                            {
                                              "success": true,
                                              "message": "이력서가 성공적으로 변환되었습니다.",
                                              "data": {
                                                "title": "백엔드 개발자 김철수",
                                                "type": "DEFAULT",
                                                "name": "김철수",
                                                "email": "kim@example.com",
                                                "birthYear": 1995,
                                                "phone": "010-1234-5678",
                                                "careerType": "EXPERIENCED",
                                                "fieldName": "백엔드 개발자",
                                                "introduction": "3년차 백엔드 개발자입니다.",
                                                "githubUrl": "https://github.com/kimcs",
                                                "blogUrl": null,
                                                "notionUrl": null,
                                                "educations": [],
                                                "techStacks": [],
                                                "customLinks": [],
                                                "careers": [],
                                                "projects": [],
                                                "trainings": [],
                                                "additionalInfos": []
                                              },
                                              "timestamp": "2025-09-11T16:24:27"
                                            }
                                            """
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "잘못된 파일 형식 또는 크기 초과",
                            content = @Content(
                                    schema = @Schema(implementation = CommonResponse.class),
                                    examples = @ExampleObject(
                                            name = "파일 오류 응답",
                                            value = """
                                            {
                                              "success": false,
                                              "message": "지원하지 않는 파일 형식입니다. PDF 또는 이미지 파일만 업로드 가능합니다.",
                                              "data": null
                                            }
                                            """
                                    )
                            )
                    ),
            }
    )
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CommonResponse<ResumeImportResponse>> importResume(
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        MDC.put("spanId", "resume-import-controller");

        String memberEmail = userDetails.getUsername();

        log.info("이력서 변환 요청 - 사용자: {}, 파일명: {}, 크기: {}bytes",
                memberEmail, file.getOriginalFilename(), file.getSize());

        try {
            ResumeImportResponse response = resumeImportService.importResume(file, memberEmail);

            log.info("이력서 변환 완료 - 사용자: {}, 변환결과: {}",
                    memberEmail, response.name());

            return ResponseEntity.ok(
                    CommonResponse.success("이력서가 성공적으로 변환되었습니다.", response)
            );

        } catch (IllegalArgumentException e) {
            log.warn("이력서 변환 실패 - 잘못된 요청: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(CommonResponse.error("VALIDATION_ERROR", e.getMessage()));

        } catch (Exception e) {
            log.error("이력서 변환 중 서버 오류: {}", e.getMessage(), e);
            log.error("오류 스택 트레이스: ", e);
            
            String errorMessage = "이력서 변환 중 오류가 발생했습니다: " + e.getMessage();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(CommonResponse.error("SERVER_ERROR", errorMessage));
        }
    }
}