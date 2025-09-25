package com.cvmento.domain.resume.controller.interfaces;

import com.cvmento.domain.resume.dto.response.ResumeImportResponse;
import com.cvmento.global.common.dto.CommonResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "이력서 변환", description = "이력서 파일을 업로드하여 CVMento 형식으로 변환")
public interface ResumeImportControllerInterface {

    @Operation(
            summary = "이력서 파일 변환",
            description = """
                    PDF 또는 이미지 형태의 이력서를 업로드하여 CVMento 이력서 형식으로 변환하고 자동으로 저장합니다.
                    
                    지원하는 파일 형식:
                    - PDF 파일 (.pdf)
                    - 이미지 파일 (.jpg, .jpeg, .png, .gif, .bmp, .webp)
                    
                    파일 크기 제한: 10MB 이하
                    
                    변환 전략:
                    - Direct: Vision API를 사용하여 파일을 직접 분석
                    - Lambda: AWS Lambda OCR을 사용하여 텍스트 추출 후 LLM 분석 (실패 시 Direct 전략으로 fallback)
                    """,
            security = @SecurityRequirement(name = "cookieAuth"),
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
                                                "educations": [
                                                  {
                                                    "schoolName": "서울대학교",
                                                    "major": "컴퓨터공학과",
                                                    "degreeLevel": "BACHELOR",
                                                    "personalGpa": 3.8,
                                                    "totalGpa": 4.5,
                                                    "graduationDate": "2024-02-15"
                                                  }
                                                ],
                                                "techStacks": [
                                                  {
                                                    "techStackId": 1,
                                                    "techStackName": "Java",
                                                    "proficiencyLevel": "ADVANCED"
                                                  },
                                                  {
                                                    "techStackId": 3,
                                                    "techStackName": "Spring Boot",
                                                    "proficiencyLevel": "INTERMEDIATE"
                                                  }
                                                ],
                                                "customLinks": [],
                                                "careers": [
                                                  {
                                                    "startDate": "2021-03-01",
                                                    "endDate": "2024-12-31",
                                                    "companyName": "네이버",
                                                    "companyDescription": "대한민국 대표 IT 기업",
                                                    "departmentPosition": "플랫폼개발팀/주임연구원",
                                                    "mainTasks": "Spring Boot 기반 REST API 개발, MySQL 데이터베이스 설계 및 최적화",
                                                    "techStacks": [
                                                      {
                                                        "techStackId": 1,
                                                        "techStackName": "Java"
                                                      }
                                                    ]
                                                  }
                                                ],
                                                "projects": [
                                                  {
                                                    "startDate": "2023-01-01",
                                                    "endDate": "2023-06-30",
                                                    "name": "전자상거래 플랫폼",
                                                    "description": "Spring Boot + React 기반 쇼핑몰 개발",
                                                    "detailedDescription": "결제 시스템 연동 및 주문 관리 기능 구현",
                                                    "repositoryUrl": "https://github.com/kimdev/ecommerce",
                                                    "deployUrl": null,
                                                    "projectType": "PERSONAL",
                                                    "techStacks": [
                                                      {
                                                        "techStackId": 1,
                                                        "techStackName": "Java",
                                                        "usageType": "백엔드 API"
                                                      }
                                                    ]
                                                  }
                                                ],
                                                "trainings": [
                                                  {
                                                    "startDate": "2023-03-01",
                                                    "endDate": "2023-09-30",
                                                    "courseName": "클라우드 아키텍처 과정",
                                                    "institutionName": "테크 아카데미",
                                                    "detailedContent": "AWS 클라우드 서비스 활용, Docker, Kubernetes 컨테이너 기술",
                                                    "techStacks": [
                                                      {
                                                        "techStackId": 15,
                                                        "techStackName": "AWS"
                                                      }
                                                    ]
                                                  }
                                                ],
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
                                    examples = {
                                            @ExampleObject(
                                                    name = "파일 형식 오류",
                                                    value = """
                                                    {
                                                      "timestamp": "2025-09-18T16:24:27",
                                                      "status": 400,
                                                      "error": "Bad Request",
                                                      "errorCode": "UNSUPPORTED_FILE_TYPE",
                                                      "message": "지원하지 않는 파일 형식입니다. 지원 형식: PDF (.pdf), 이미지 (.jpg, .jpeg, .png)",
                                                      "errors": {}
                                                    }
                                                    """
                                            ),
                                            @ExampleObject(
                                                    name = "파일 크기 초과",
                                                    value = """
                                                    {
                                                      "timestamp": "2025-09-18T16:24:27",
                                                      "status": 400,
                                                      "error": "Bad Request",
                                                      "errorCode": "FILE_SIZE_EXCEEDED",
                                                      "message": "파일 크기가 제한을 초과했습니다. 최대 크기: 10MB",
                                                      "errors": {}
                                                    }
                                                    """
                                            ),
                                            @ExampleObject(
                                                    name = "빈 파일 오류",
                                                    value = """
                                                    {
                                                      "timestamp": "2025-09-18T16:24:27",
                                                      "status": 400,
                                                      "error": "Bad Request",
                                                      "errorCode": "INVALID_FILE",
                                                      "message": "파일이 비어있습니다. 이력서 내용이 포함된 유효한 파일을 업로드해주세요.",
                                                      "errors": {}
                                                    }
                                                    """
                                            )
                                    }
                            )
                    ),
                    @ApiResponse(
                            responseCode = "422",
                            description = "이력서로 인식할 수 없는 파일",
                            content = @Content(
                                    schema = @Schema(implementation = CommonResponse.class),
                                    examples = @ExampleObject(
                                            name = "이력서가 아닌 파일",
                                            value = """
                                            {
                                              "timestamp": "2025-09-18T16:24:27",
                                              "status": 422,
                                              "error": "Unprocessable Entity",
                                              "errorCode": "AI_INVALID_REQUEST",
                                              "message": "이력서 형태로 인식할 수 없는 파일입니다. 명확한 이력서 내용이 포함된 파일을 업로드해주세요.",
                                              "errors": {}
                                            }
                                            """
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "429",
                            description = "사용 한도 초과 (토큰 부족)",
                            content = @Content(
                                    schema = @Schema(implementation = CommonResponse.class),
                                    examples = @ExampleObject(
                                            name = "토큰 부족",
                                            value = """
                                            {
                                              "timestamp": "2025-09-18T16:24:27",
                                              "status": 429,
                                              "error": "Too Many Requests",
                                              "errorCode": "USAGE_LIMIT_EXCEEDED",
                                              "message": "이력서 변환 토큰이 부족합니다. 토큰을 충전해주세요.",
                                              "errors": {
                                                "usageType": "RESUME_FILE_CONVERT",
                                                "remainingTokens": "0",
                                                "requiredTokens": "5",
                                                "nextRefillTime": "2025-09-18T18:00:00"
                                              }
                                            }
                                            """
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "이력서 변환 실패 (LLM API 오류, 파싱 실패 등)",
                            content = @Content(
                                    schema = @Schema(implementation = CommonResponse.class),
                                    examples = {
                                            @ExampleObject(
                                                    name = "Vision API 처리 실패",
                                                    value = """
                                                    {
                                                      "timestamp": "2025-09-18T16:24:27",
                                                      "status": 500,
                                                      "error": "Internal Server Error",
                                                      "errorCode": "RESUME_CONVERSION_ERROR",
                                                      "message": "이력서 파일 분석 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.",
                                                      "errors": {}
                                                    }
                                                    """
                                            ),
                                            @ExampleObject(
                                                    name = "Lambda OCR 실패",
                                                    value = """
                                                    {
                                                      "timestamp": "2025-09-18T16:24:27",
                                                      "status": 500,
                                                      "error": "Internal Server Error",
                                                      "errorCode": "LAMBDA_SERVICE_ERROR",
                                                      "message": "파일 텍스트 추출 중 오류가 발생했습니다. 파일을 확인하고 다시 시도해주세요.",
                                                      "errors": {}
                                                    }
                                                    """
                                            ),
                                            @ExampleObject(
                                                    name = "LLM 응답 파싱 실패",
                                                    value = """
                                                    {
                                                      "timestamp": "2025-09-18T16:24:27",
                                                      "status": 500,
                                                      "error": "Internal Server Error",
                                                      "errorCode": "RESUME_CONVERSION_ERROR",
                                                      "message": "이력서 변환 결과 처리 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.",
                                                      "errors": {}
                                                    }
                                                    """
                                            )
                                    }
                            )
                    )
            }
    )
    ResponseEntity<CommonResponse<ResumeImportResponse>> importResume(
            @Parameter(
                    description = "변환할 이력서 파일 (PDF 또는 이미지)",
                    required = true,
                    content = @Content(mediaType = "multipart/form-data")
            )
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal UserDetails userDetails
    );
}