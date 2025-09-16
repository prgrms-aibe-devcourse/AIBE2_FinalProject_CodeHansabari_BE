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
                                                      "success": false,
                                                      "message": "지원하지 않는 파일 형식입니다. PDF 또는 이미지 파일만 업로드 가능합니다.",
                                                      "data": null,
                                                      "timestamp": "2025-09-11T16:24:27"
                                                    }
                                                    """
                                            ),
                                            @ExampleObject(
                                                    name = "파일 크기 초과",
                                                    value = """
                                                    {
                                                      "success": false,
                                                      "message": "파일 크기는 10MB를 초과할 수 없습니다.",
                                                      "data": null,
                                                      "timestamp": "2025-09-11T16:24:27"
                                                    }
                                                    """
                                            ),
                                            @ExampleObject(
                                                    name = "빈 파일 오류",
                                                    value = """
                                                    {
                                                      "success": false,
                                                      "message": "파일이 비어있습니다.",
                                                      "data": null,
                                                      "timestamp": "2025-09-11T16:24:27"
                                                    }
                                                    """
                                            )
                                    }
                            )
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "이력서 변환 실패 (LLM API 오류, 파싱 실패 등)",
                            content = @Content(
                                    schema = @Schema(implementation = CommonResponse.class),
                                    examples = {
                                            @ExampleObject(
                                                    name = "LLM API 오류",
                                                    value = """
                                                    {
                                                      "success": false,
                                                      "message": "이력서 변환 중 오류가 발생했습니다: 이력서 변환에 실패했습니다.",
                                                      "data": null,
                                                      "timestamp": "2025-09-11T16:24:27"
                                                    }
                                                    """
                                            ),
                                            @ExampleObject(
                                                    name = "Lambda OCR 오류",
                                                    value = """
                                                    {
                                                      "success": false,
                                                      "message": "이력서 변환 중 오류가 발생했습니다: OCR 처리 중 오류가 발생했습니다.",
                                                      "data": null,
                                                      "timestamp": "2025-09-11T16:24:27"
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