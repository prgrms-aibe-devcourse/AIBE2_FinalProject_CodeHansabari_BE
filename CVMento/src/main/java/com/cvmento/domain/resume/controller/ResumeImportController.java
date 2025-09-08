package com.cvmento.domain.resume.controller;

import com.cvmento.domain.resume.dto.response.ResumeResponse;
import com.cvmento.domain.resume.service.ResumeImportService;
import com.cvmento.global.common.dto.CommonResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "이력서 파일 가져오기", description = "파일(PDF, PNG)로부터 이력서 자동 생성 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/resumes/import")
public class ResumeImportController {

    private final ResumeImportService resumeImportService;

    @Operation(
            summary = "이력서 파일 업로드로 생성",
            description = "PDF 또는 이미지 파일을 업로드하여 이력서를 자동으로 생성합니다. 이 API는 Vision LLM을 사용하여 파일 내용을 분석하고 이력서 형식으로 변환합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "이력서 파일 생성 성공",
                            content = @Content(
                                    schema = @Schema(implementation = CommonResponse.class),
                                    examples = @ExampleObject(
                                            name = "파일 생성 성공",
                                            value = """
                                                    {
                                                      "success": true,
                                                      "message": "이력서 파일로부터 생성 성공",
                                                      "data": {
                                                        "resumeId": 1,
                                                        "title": "파일에서 추출된 이력서",
                                                        "memberName": "홍길동",
                                                        "memberEmail": "hong@example.com",
                                                        "selfIntroduction": "파일에서 추출된 자기소개입니다.",
                                                        "techStack": "Java,Spring,MySQL",
                                                        "createdAt": "2025-09-05T14:30:00"
                                                      }
                                                    }
                                                    """
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "잘못된 파일",
                            content = @Content(
                                    schema = @Schema(implementation = java.util.Map.class),
                                    examples = {
                                            @ExampleObject(
                                                    name = "빈 파일",
                                                    value = """
                                                            {
                                                              "timestamp": "2025-09-05T14:30:00",
                                                              "status": 400,
                                                              "error": "Bad Request",
                                                              "errorCode": "VALIDATION_ERROR",
                                                              "message": "Uploaded file is empty.",
                                                              "errors": {}
                                                            }
                                                            """
                                            ),
                                            @ExampleObject(
                                                    name = "지원하지 않는 파일 형식",
                                                    value = """
                                                            {
                                                              "timestamp": "2025-09-05T14:30:00",
                                                              "status": 400,
                                                              "error": "Bad Request",
                                                              "errorCode": "VALIDATION_ERROR",
                                                              "message": "Invalid file type. Only PDF, PNG, JPG files are allowed.",
                                                              "errors": {}
                                                            }
                                                            """
                                            ),
                                            @ExampleObject(
                                                    name = "파일 크기 초과",
                                                    value = """
                                                            {
                                                              "timestamp": "2025-09-05T14:30:00",
                                                              "status": 400,
                                                              "error": "Bad Request",
                                                              "errorCode": "VALIDATION_ERROR",
                                                              "message": "File size exceeds the limit of 5MB.",
                                                              "errors": {}
                                                            }
                                                            """
                                            )
                                    }
                            )
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "AI 처리 실패",
                            content = @Content(
                                    schema = @Schema(implementation = java.util.Map.class),
                                    examples = {
                                            @ExampleObject(
                                                    name = "텍스트 추출 실패",
                                                    value = """
                                                            {
                                                              "timestamp": "2025-09-05T14:30:00",
                                                              "status": 500,
                                                              "error": "Internal Server Error",
                                                              "errorCode": "RESUME_AI_SERVICE_ERROR",
                                                              "message": "파일에서 텍스트를 추출하지 못했습니다.",
                                                              "errors": {}
                                                            }
                                                            """
                                            ),
                                            @ExampleObject(
                                                    name = "AI 분석 실패",
                                                    value = """
                                                            {
                                                              "timestamp": "2025-09-05T14:30:00",
                                                              "status": 500,
                                                              "error": "Internal Server Error",
                                                              "errorCode": "RESUME_AI_SERVICE_ERROR",
                                                              "message": "AI가 이력서 내용을 분석하지 못했습니다.",
                                                              "errors": {}
                                                            }
                                                            """
                                            )
                                    }
                            )
                    ),
                    @ApiResponse(
                            responseCode = "502",
                            description = "AI 서비스 연결 실패",
                            content = @Content(
                                    schema = @Schema(implementation = java.util.Map.class),
                                    examples = @ExampleObject(
                                            name = "Vision LLM 연결 실패",
                                            value = """
                                                    {
                                                      "timestamp": "2025-09-05T14:30:00",
                                                      "status": 502,
                                                      "error": "Bad Gateway",
                                                      "errorCode": "RESUME_AI_CONNECTION_ERROR",
                                                      "message": "AI 서비스에 연결할 수 없습니다. 네트워크 상태를 확인하거나 잠시 후 다시 시도해주세요.",
                                                      "errors": {}
                                                    }
                                                    """
                                    )
                            )
                    )
            }
    )
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CommonResponse<ResumeResponse>> importResumeFromFile(
            @RequestPart("file") MultipartFile file,
            @AuthenticationPrincipal UserDetails userDetails) {
        ResumeResponse resumeResponse = resumeImportService.createResumeFromFile(file, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(CommonResponse.success("이력서 파일로부터 생성 성공", resumeResponse));
    }
}
