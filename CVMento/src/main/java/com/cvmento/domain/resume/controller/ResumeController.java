package com.cvmento.domain.resume.controller;

import com.cvmento.domain.resume.dto.request.ResumeCreateRequest;
import com.cvmento.domain.resume.dto.request.ResumeUpdateRequest;
import com.cvmento.domain.resume.dto.response.ResumeResponse;
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

import java.util.List;

@Tag(name = "이력서 관리", description = "이력서 CRUD API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/resumes")
public class ResumeController {

    private final ResumeService resumeService;

    @Operation(
            summary = "이력서 생성",
            description = "새로운 이력서를 생성합니다.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "생성할 이력서 내용",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = ResumeCreateRequest.class),
                            examples = @ExampleObject(
                                    value = """
{
  "title": "신입 백엔드 개발자 이력서",
  "memberInfo": {
    "name": "홍길동",
    "email": "hong.gildong@example.com",
    "phoneNumber": "010-1234-5678",
    "blogUrl": "https://honggildong.dev"
  },
  "intro": {
    "selfIntroduction": "신입 백엔드 개발자 홍길동입니다. 컴퓨터공학을 전공하며 웹 개발에 관심을 가지게 되었고, 특히 서버 사이드 개발과 데이터베이스 설계에 열정을 가지고 있습니다. 사용자에게 안정적이고 효율적인 서비스를 제공하는 백엔드 시스템을 구축하는 것이 목표입니다. 새로운 기술 학습에 대한 열의가 높으며, 팀워크를 중시하는 개발자가 되고자 합니다.",
    "techStack": ["Java", "Python", "JavaScript", "Spring Boot", "Spring MVC", "Spring Data JPA", "Express.js", "MySQL", "PostgreSQL", "Redis", "IntelliJ IDEA", "VS Code", "Git", "Docker", "Postman", "AWS EC2", "RDS", "GitHub Actions", "Nginx"]
  },
  "sections": [
    {
      "sectionType": "EDUCATION",
      "sectionTitle": "학력",
      "items": [
        {
          "title": "ABC 대학교",
          "subTitle": "컴퓨터공학과",
          "startDate": "2018-03-01",
          "endDate": "2022-02-28",
          "description": "학사 졸업"
        }
      ]
    },
    {
      "sectionType": "WORK_EXPERIENCE",
      "sectionTitle": "수정된 경력",
      "items": [
        {
          "title": "XYZ 소프트웨어",
          "subTitle": "백엔드 개발 인턴 (정규직 전환)",
          "startDate": "2023-01-01",
          "endDate": "2023-12-31",
          "description": "Spring Boot를 이용한 RESTful API 개발 및 배포 참여"
        }
      ]
    }
  ]
}
"""
                            )
                    )
            )
    )
    @PostMapping
    public ResponseEntity<CommonResponse<ResumeResponse>> createResume(
            @Valid @RequestBody ResumeCreateRequest resumeCreateRequest,
            @AuthenticationPrincipal UserDetails userDetails) {
        ResumeResponse resumeResponse = resumeService.createResume(resumeCreateRequest, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(CommonResponse.success("이력서 생성 성공", resumeResponse));
    }

    @Operation(
            summary = "이력서 단건 조회",
            description = "특정 이력서 ID로 이력서 상세 정보를 조회합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "이력서 조회 성공",
                            content = @Content(
                                    schema = @Schema(implementation = CommonResponse.class),
                                    examples = @ExampleObject(
                                            name = "조회 성공",
                                            value = """
                                                    {
                                                      "success": true,
                                                      "message": "이력서 조회 성공",
                                                      "data": {
                                                        "resumeId": 1,
                                                        "title": "신입 백엔드 개발자 이력서",
                                                        "memberName": "홍길동",
                                                        "memberEmail": "hong.gildong@example.com",
                                                        "memberPhoneNumber": "010-1234-5678",
                                                        "selfIntroduction": "열정적인 신입 개발자입니다.",
                                                        "techStack": "Java,Spring,MySQL",
                                                        "sections": [],
                                                        "createdAt": "2025-09-05T14:30:00",
                                                        "updatedAt": "2025-09-05T14:30:00"
                                                      }
                                                    }
                                                    """
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "이력서를 찾을 수 없음",
                            content = @Content(
                                    schema = @Schema(implementation = java.util.Map.class),
                                    examples = @ExampleObject(
                                            name = "이력서 없음",
                                            value = """
                                                    {
                                                      "timestamp": "2025-09-05T14:30:00",
                                                      "status": 404,
                                                      "error": "Not Found",
                                                      "errorCode": "RESUME_NOT_FOUND",
                                                      "message": "이력서를 찾을 수 없습니다.",
                                                      "errors": {}
                                                    }
                                                    """
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "사용자를 찾을 수 없음",
                            content = @Content(
                                    schema = @Schema(implementation = java.util.Map.class),
                                    examples = @ExampleObject(
                                            name = "사용자 없음",
                                            value = """
                                                    {
                                                      "timestamp": "2025-09-05T14:30:00",
                                                      "status": 404,
                                                      "error": "Not Found",
                                                      "errorCode": "MEMBER_NOT_FOUND",
                                                      "message": "사용자를 찾을 수 없습니다.",
                                                      "errors": {}
                                                    }
                                                    """
                                    )
                            )
                    )
            }
    )
    @GetMapping("/{resumeId}")
    public ResponseEntity<CommonResponse<ResumeResponse>> getResumeById(
            @PathVariable Long resumeId,
            @AuthenticationPrincipal UserDetails userDetails) {
        ResumeResponse resumeResponse = resumeService.getResume(resumeId, userDetails.getUsername());
        return ResponseEntity.ok(CommonResponse.success("이력서 조회 성공", resumeResponse));
    }

    @Operation(
            summary = "사용자 이력서 목록 조회",
            description = "현재 로그인한 사용자의 모든 이력서 목록을 조회합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "이력서 목록 조회 성공",
                            content = @Content(
                                    schema = @Schema(implementation = CommonResponse.class),
                                    examples = {
                                            @ExampleObject(
                                                    name = "이력서가 있는 경우",
                                                    value = """
                                                            {
                                                              "success": true,
                                                              "message": "이력서 목록 조회 성공",
                                                              "data": [
                                                                {
                                                                  "resumeId": 1,
                                                                  "title": "신입 백엔드 개발자 이력서",
                                                                  "memberName": "홍길동",
                                                                  "createdAt": "2025-09-05T14:30:00"
                                                                },
                                                                {
                                                                  "resumeId": 2,
                                                                  "title": "시니어 개발자 이력서",
                                                                  "memberName": "홍길동",
                                                                  "createdAt": "2025-09-06T10:15:00"
                                                                }
                                                              ]
                                                            }
                                                            """
                                            ),
                                            @ExampleObject(
                                                    name = "이력서가 없는 경우",
                                                    value = """
                                                            {
                                                              "success": true,
                                                              "message": "이력서 목록 조회 성공",
                                                              "data": []
                                                            }
                                                            """
                                            )
                                    }
                            )
                    )
            }
    )
    @GetMapping
    public ResponseEntity<CommonResponse<List<ResumeResponse>>> getResumesByUser(@AuthenticationPrincipal UserDetails userDetails) {
        List<ResumeResponse> resumes = resumeService.getResumesByMember(userDetails.getUsername());
        return ResponseEntity.ok(CommonResponse.success("이력서 목록 조회 성공", resumes));
    }

    @Operation(
            summary = "사용자 이력서 수정",
            description = "현재 로그인한 사용자의 이력서를 수정합니다.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "수정할 이력서 내용",
                    required = true,
                    content = @Content(
                            schema = @Schema(implementation = ResumeUpdateRequest.class),
                            examples = @ExampleObject(
                                    name = "이력서 수정 예시",
                                    value = """
{
  "title": "백엔드 개발자 이력서 (업데이트)",
  "memberInfo": {
    "name": "홍길동",
    "email": "hong.gildong@example.com",
    "phoneNumber": "010-1234-5678",
    "blogUrl": "https://honggildong.dev"
  },
  "intro": {
    "selfIntroduction": "신입 백엔드 개발자 홍길동입니다. 컴퓨터공학을 전공하며 웹 개발에 관심을 가지게 되었고, 특히 서버 사이드 개발과 데이터베이스 설계에 열정을 가지고 있습니다. 사용자에게 안정적이고 효율적인 서비스를 제공하는 백엔드 시스템을 구축하는 것이 목표입니다. 새로운 기술 학습에 대한 열의가 높으며, 팀워크를 중시하는 개발자가 되고자 합니다.",
    "techStack": ["Java", "Python",  "PostgreSQL"]
  },
  "sections": [
    {
      "sectionType": "EDUCATION",
      "sectionTitle": "학력",
      "items": [
        {
          "title": "ABC 대학교",
          "subTitle": "컴퓨터공학과",
          "startDate": "2018-03-01",
          "endDate": "2022-02-28",
          "description": "학사 졸업 (우수 졸업)"
        }
      ]
    },
    {
      "sectionType": "WORK_EXPERIENCE",
      "sectionTitle": "경력",
      "items": [
        {
          "title": "XYZ 소프트웨어",
          "subTitle": "백엔드 개발자",
          "startDate": "2023-01-01",
          "endDate": "2024-12-31",
          "description": "Spring Boot 기반 REST API 개발, 배포 자동화, 성능 최적화"
        }
      ]
    },
    {
      "sectionType": "PROJECT",
      "sectionTitle": "프로젝트",
      "items": [
        {
          "title": "이력서 관리 서비스",
          "subTitle": "개인 프로젝트",
          "startDate": "2024-03-01",
          "endDate": "2024-06-30",
          "description": "스프링 시큐리티/데이터 JPA 적용, AWS 배포"
        }
      ]
    },
    {
      "sectionType": "SKILL",
      "sectionTitle": "기술 스택",
      "items": [
        {
          "title": "Backend",
          "subTitle": "Java, Spring Boot, JPA, QueryDSL",
          "startDate": null,
          "endDate": null,
          "description": "테스트 코드(JUnit5), CI/CD(GitHub Actions)"
        }
      ]
    }
  ]
}
"""
                            )
                    )
            )
    )
    @PutMapping("/{resumeId}")
    public ResponseEntity<CommonResponse<ResumeResponse>> updateResume(
            @PathVariable Long resumeId,
            @Valid @RequestBody ResumeUpdateRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        ResumeResponse resumeResponse = resumeService.updateResume(resumeId, request, userDetails.getUsername());
        return ResponseEntity.ok(CommonResponse.success("이력서 수정 성공", resumeResponse));
    }


    @Operation(
            summary = "이력서 삭제 (소프트 삭제)",
            description = "특정 이력서 ID를 사용하여 이력서를 소프트 삭제합니다. 실제 데이터는 삭제되지 않고 상태만 변경됩니다."
    )
    @DeleteMapping("/{resumeId}")
    public ResponseEntity<CommonResponse<Void>> deleteResume(
            @PathVariable Long resumeId,
            @AuthenticationPrincipal UserDetails userDetails) {
        resumeService.deleteResume(resumeId, userDetails.getUsername());
        return ResponseEntity.ok(CommonResponse.success("이력서 삭제 성공"));
    }
}