package com.cvmento.domain.resume.controller.interfaces;

import com.cvmento.domain.resume.dto.request.ResumeSaveRequest;
import com.cvmento.domain.resume.dto.request.ResumeUpdateRequest;
import com.cvmento.domain.resume.dto.response.ResumeDetailResponse;
import com.cvmento.domain.resume.dto.response.ResumeThumbnailResponse;
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
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "이력서 관리", description = "이력서 CRUD API - 저장, 수정, 삭제, 조회 기능 제공")
public interface ResumeControllerInterface {

    @Operation(
            summary = "이력서 저장",
            description = "새로운 이력서를 저장합니다. 이력서 제목과 기본 정보, 학력, 경력, 프로젝트 등 모든 정보를 포함합니다.",
            security = @SecurityRequirement(name = "cookieAuth"),
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "저장할 이력서 전체 정보",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = ResumeSaveRequest.class),
                            examples = {
                                    @ExampleObject(
                                            name = "신입 개발자 이력서",
                                            description = "신입 개발자의 기본 이력서 예시",
                                            value = """
                                                    {
                                                      "title": "네이버 백엔드 개발자 지원용 이력서",
                                                      "type": "DEFAULT",
                                                      "name": "김개발",
                                                      "email": "kim@example.com",
                                                      "birthYear": 1995,
                                                      "phone": "010-1234-5678",
                                                      "careerType": "FRESHMAN",
                                                      "fieldName": "백엔드 개발자",
                                                      "introduction": "열정적인 신입 개발자입니다.",
                                                      "githubUrl": "https://github.com/kimdev",
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
                                                          "proficiencyLevel": "INTERMEDIATE"
                                                        }
                                                      ],
                                                      "customLinks": [],
                                                      "careers": [],
                                                      "projects": [],
                                                      "trainings": [],
                                                      "additionalInfos": []
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "경력 개발자 이력서",
                                            description = "경력 개발자의 상세 이력서 예시",
                                            value = """
                                                    {
                                                      "title": "카카오 시니어 개발자 지원용 이력서",
                                                      "type": "MODERN",
                                                      "name": "박경력",
                                                      "email": "park@example.com",
                                                      "birthYear": 1990,
                                                      "phone": "010-9876-5432",
                                                      "careerType": "EXPERIENCED",
                                                      "fieldName": "풀스택 개발자",
                                                      "introduction": "5년 경력의 풀스택 개발자입니다.",
                                                      "githubUrl": "https://github.com/parkdev",
                                                      "blogUrl": "https://parkdev.blog",
                                                      "notionUrl": "https://notion.so/parkdev",
                                                      "educations": [],
                                                      "techStacks": [],
                                                      "customLinks": [],
                                                      "careers": [
                                                        {
                                                          "startDate": "2020-01-01",
                                                          "endDate": "2024-12-31",
                                                          "companyName": "네이버",
                                                          "companyDescription": "대한민국 대표 IT 기업",
                                                          "departmentPosition": "플랫폼개발팀/시니어 개발자",
                                                          "mainTasks": "Spring Boot 기반 대용량 트래픽 처리 시스템 개발",
                                                          "techStacks": []
                                                        }
                                                      ],
                                                      "projects": [],
                                                      "trainings": [],
                                                      "additionalInfos": []
                                                    }
                                                    """
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
                                            value = """
                                                    {
                                                      "success": true,
                                                      "message": "이력서가 성공적으로 저장되었습니다.",
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
                                                        "title": "이력서 제목은 필수입니다.",
                                                        "email": "올바른 이메일 형식이 아닙니다."
                                                      }
                                                    }
                                                    """
                                    )
                            )
                    )
            }
    )
    ResponseEntity<CommonResponse<Void>> saveResume(
            @Valid @RequestBody ResumeSaveRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    );

    @Operation(
            summary = "이력서 수정",
            description = "기존 이력서를 전체 덮어쓰기 방식으로 수정합니다. 모든 하위 데이터(학력, 경력, 프로젝트 등)가 새로운 데이터로 완전히 교체됩니다.",
            security = @SecurityRequirement(name = "cookieAuth"),
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "수정할 이력서 전체 정보",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = ResumeUpdateRequest.class),
                            examples = @ExampleObject(
                                    name = "완전한 이력서 수정",
                                    description = "모든 섹션이 포함된 이력서 수정 예시",
                                    value = """
                                            {
                                              "title": "카카오 시니어 백엔드 개발자 지원용 이력서",
                                              "type": "DEFAULT",
                                              "name": "김개발",
                                              "email": "kim.dev@example.com",
                                              "birthYear": 1995,
                                              "phone": "010-1234-5678",
                                              "careerType": "EXPERIENCED",
                                              "fieldName": "시니어 백엔드 개발자",
                                              "introduction": "4년 경력의 백엔드 개발자로 성장했습니다. 현재는 팀 리드 역할을 수행하며 아키텍처 설계에 관심이 많습니다.",
                                              "githubUrl": "https://github.com/kimdev",
                                              "blogUrl": "https://kimdev.tistory.com",
                                              "notionUrl": "https://notion.so/kimdev-portfolio",
                                              "educations": [
                                                {
                                                  "schoolName": "서울대학교",
                                                  "major": "컴퓨터공학과",
                                                  "degreeLevel": "MASTER",
                                                  "personalGpa": 4.0,
                                                  "totalGpa": 4.5,
                                                  "graduationDate": "2023-02-15"
                                                }
                                              ],
                                              "techStacks": [
                                                {
                                                  "techStackId": 1,
                                                  "proficiencyLevel": "ADVANCED"
                                                },
                                                {
                                                  "techStackId": 3,
                                                  "proficiencyLevel": "ADVANCED"
                                                }
                                              ],
                                              "customLinks": [
                                                {
                                                  "name": "기술 블로그",
                                                  "url": "https://tech-blog.kimdev.com"
                                                }
                                              ],
                                              "careers": [
                                                {
                                                  "startDate": "2021-03-01",
                                                  "endDate": "2024-12-31",
                                                  "companyName": "네이버",
                                                  "companyDescription": "대한민국 대표 IT 기업",
                                                  "departmentPosition": "플랫폼개발팀/팀리드",
                                                  "mainTasks": "마이크로서비스 아키텍처 설계 및 팀 관리, 시스템 성능 최적화로 TPS 300% 향상",
                                                  "techStacks": [
                                                    {
                                                      "techStackId": 1
                                                    },
                                                    {
                                                      "techStackId": 3
                                                    }
                                                  ]
                                                }
                                              ],
                                              "projects": [
                                                {
                                                  "startDate": "2024-01-01",
                                                  "endDate": "2024-06-30",
                                                  "name": "실시간 채팅 시스템",
                                                  "description": "WebSocket 기반 대용량 실시간 채팅 플랫폼",
                                                  "detailedDescription": "동시 접속자 10만명을 지원하는 확장 가능한 실시간 채팅 시스템. Redis Cluster와 Kafka를 활용한 메시지 큐 시스템 구축",
                                                  "repositoryUrl": "https://github.com/kimdev/realtime-chat",
                                                  "deployUrl": "https://chat.kimdev.com",
                                                  "projectType": "COMPANY",
                                                  "techStacks": [
                                                    {
                                                      "techStackId": 1,
                                                      "usageType": "백엔드 API"
                                                    }
                                                  ]
                                                }
                                              ],
                                              "trainings": [
                                                {
                                                  "startDate": "2023-01-01",
                                                  "endDate": "2023-03-31",
                                                  "courseName": "AWS Solutions Architect 자격증 과정",
                                                  "institutionName": "AWS 교육센터",
                                                  "detailedContent": "클라우드 아키텍처 설계 및 AWS 서비스 활용 방법 학습",
                                                  "techStacks": []
                                                }
                                              ],
                                              "additionalInfos": [
                                                {
                                                  "startDate": "2024-05-20",
                                                  "endDate": "2024-05-20",
                                                  "category": "CERTIFICATE",
                                                  "activityName": "AWS Solutions Architect",
                                                  "relatedOrganization": "Amazon Web Services",
                                                  "detailedContent": "AWS Solutions Architect Associate 자격증 취득",
                                                  "certificateNumber": "AWS-SAA-2024-001234",
                                                  "languageLevel": null
                                                }
                                              ]
                                            }
                                            """
                            )
                    )
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "이력서 수정 성공",
                            content = @Content(
                                    schema = @Schema(implementation = CommonResponse.class),
                                    examples = @ExampleObject(
                                            name = "이력서 수정 성공 응답",
                                            value = """
                                                    {
                                                      "success": true,
                                                      "message": "이력서가 성공적으로 수정되었습니다.",
                                                      "data": null,
                                                      "timestamp": "2024-01-15T14:30:00"
                                                    }
                                                    """
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "이력서를 찾을 수 없음",
                            content = @Content(schema = @Schema(implementation = CommonResponse.class))
                    )
            }
    )
    ResponseEntity<CommonResponse<Void>> updateResume(
            @Parameter(description = "이력서 ID") @PathVariable Long resumeId,
            @Valid @RequestBody ResumeUpdateRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    );

    @Operation(
            summary = "이력서 삭제",
            description = "이력서를 소프트 삭제합니다. 실제 데이터는 삭제되지 않고 상태만 DELETED로 변경됩니다.",
            security = @SecurityRequirement(name = "cookieAuth"),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "이력서 삭제 성공",
                            content = @Content(
                                    schema = @Schema(implementation = CommonResponse.class),
                                    examples = @ExampleObject(
                                            name = "이력서 삭제 성공 응답",
                                            value = """
                                                    {
                                                      "success": true,
                                                      "message": "이력서가 성공적으로 삭제되었습니다.",
                                                      "data": null,
                                                      "timestamp": "2024-01-15T14:30:00"
                                                    }
                                                    """
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "이력서를 찾을 수 없음",
                            content = @Content(schema = @Schema(implementation = CommonResponse.class))
                    )
            }
    )
    ResponseEntity<CommonResponse<Void>> deleteResume(
            @Parameter(description = "삭제할 이력서 ID", example = "1") @PathVariable Long resumeId,
            @AuthenticationPrincipal UserDetails userDetails
    );

    @Operation(
            summary = "이력서 목록 조회",
            description = "사용자의 활성 상태 이력서 목록을 페이징하여 조회합니다.",
            security = @SecurityRequirement(name = "cookieAuth"),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "이력서 목록 조회 성공",
                            content = @Content(
                                    schema = @Schema(implementation = CommonResponse.class),
                                    examples = @ExampleObject(
                                            name = "이력서 목록 조회 성공",
                                            value = """
                                                    {
                                                      "success": true,
                                                      "message": "이력서 목록을 성공적으로 조회했습니다.",
                                                      "data": {
                                                        "content": [
                                                          {
                                                            "resumeId": 1,
                                                            "title": "네이버 백엔드 개발자 지원용 이력서",
                                                            "updatedAt": "2024-03-15T10:30:25",
                                                            "completedSections": ["educations", "techStacks", "careers", "projects"]
                                                          }
                                                        ],
                                                        "totalPages": 1,
                                                        "totalElements": 1,
                                                        "size": 10,
                                                        "number": 0
                                                      },
                                                      "timestamp": "2024-01-15T14:30:00"
                                                    }
                                                    """
                                    )
                            )
                    )
            }
    )
    ResponseEntity<CommonResponse<Page<ResumeThumbnailResponse>>> getResumeList(
            @AuthenticationPrincipal UserDetails userDetails,
            @Parameter(description = "페이지 번호 (0부터 시작)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기", example = "10")
            @RequestParam(defaultValue = "10") int size
    );

    @Operation(
            summary = "이력서 상세 조회",
            description = "이력서 ID로 해당 이력서의 모든 상세 정보를 조회합니다.",
            security = @SecurityRequirement(name = "cookieAuth"),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "이력서 상세 조회 성공",
                            content = @Content(
                                    schema = @Schema(implementation = CommonResponse.class),
                                    examples = @ExampleObject(
                                            name = "이력서 상세 조회 성공",
                                            value = """
                                                    {
                                                      "success": true,
                                                      "message": "이력서를 성공적으로 조회했습니다.",
                                                      "data": {
                                                        "resumeId": 1,
                                                        "title": "네이버 백엔드 개발자 지원용 이력서",
                                                        "type": "DEFAULT",
                                                        "name": "김개발",
                                                        "email": "kim@example.com",
                                                        "birthYear": 1995,
                                                        "phone": "010-1234-5678",
                                                        "careerType": "FRESHMAN",
                                                        "fieldName": "백엔드 개발자",
                                                        "introduction": "열정적인 개발자입니다.",
                                                        "githubUrl": "https://github.com/kimdev",
                                                        "blogUrl": null,
                                                        "notionUrl": null,
                                                        "createdAt": "2024-03-10T09:30:00",
                                                        "updatedAt": "2024-03-15T10:30:25",
                                                        "educations": [],
                                                        "techStacks": [],
                                                        "customLinks": [],
                                                        "careers": [],
                                                        "projects": [],
                                                        "trainings": [],
                                                        "additionalInfos": []
                                                      },
                                                      "timestamp": "2024-01-15T14:30:00"
                                                    }
                                                    """
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "이력서를 찾을 수 없음",
                            content = @Content(schema = @Schema(implementation = CommonResponse.class))
                    )
            }
    )
    ResponseEntity<CommonResponse<ResumeDetailResponse>> getResumeDetail(
            @Parameter(description = "이력서 ID", example = "1") @PathVariable Long resumeId,
            @AuthenticationPrincipal UserDetails userDetails
    );

    @Operation(
            summary = "이력서 복구 (관리자 전용)",
            description = "소프트 삭제된 이력서를 관리자 권한으로 복구합니다. 실제 데이터는 삭제되지 않고 상태만 ACTIVE로 변경됩니다.",
            security = @SecurityRequirement(name = "cookieAuth"),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "이력서 복구 성공",
                            content = @Content(
                                    schema = @Schema(implementation = CommonResponse.class),
                                    examples = @ExampleObject(
                                            name = "이력서 복구 성공 응답",
                                            value = """
                                                    {
                                                      "success": true,
                                                      "message": "이력서가 복구되었습니다.",
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
                            description = "복구할 이력서를 찾을 수 없음",
                            content = @Content(schema = @Schema(implementation = CommonResponse.class))
                    )
            }
    )
    ResponseEntity<CommonResponse<Void>> restoreResume(
            @Parameter(description = "복구할 이력서 ID") @PathVariable Long resumeId,
            @AuthenticationPrincipal UserDetails userDetails
    );
}