package com.cvmento.domain.resume.controller;

import com.cvmento.domain.resume.dto.response.ResumeMetadataResponse;
import com.cvmento.domain.resume.service.ResumeMetadataService;
import com.cvmento.global.common.dto.CommonResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "이력서 메타데이터", description = "이력서 작성에 필요한 메타데이터 API")
@RestController
@RequestMapping("/api/v1/resume-metadata")
@RequiredArgsConstructor
@Slf4j
public class ResumeMetadataController {

    private final ResumeMetadataService resumeMetadataService;

    /**
     * 이력서 작성용 메타데이터 조회
     */
    @Operation(
            summary = "이력서 메타데이터 조회",
            description = "이력서 작성 시 필요한 모든 메타데이터(기술스택, Enum 값 등)를 조회합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "메타데이터 조회 성공",
                            content = @Content(
                                    schema = @Schema(implementation = CommonResponse.class),
                                    examples = @ExampleObject(
                                            name = "메타데이터 조회 성공",
                                            value = """
                                            {
                                              "success": true,
                                              "message": "이력서 메타데이터를 성공적으로 조회했습니다.",
                                              "data": {
                                                "techStacks": [
                                                  {
                                                    "id": 1,
                                                    "name": "Java",
                                                    "category": "Language"
                                                  },
                                                  {
                                                    "id": 2,
                                                    "name": "JavaScript",
                                                    "category": "Language"
                                                  },
                                                  {
                                                    "id": 3,
                                                    "name": "Spring Boot",
                                                    "category": "Backend"
                                                  },
                                                  {
                                                    "id": 4,
                                                    "name": "React",
                                                    "category": "Frontend"
                                                  },
                                                  {
                                                    "id": 5,
                                                    "name": "MySQL",
                                                    "category": "Database"
                                                  }
                                                ],
                                                "resumeTypes": [
                                                  {
                                                    "value": "DEFAULT",
                                                    "description": "기본형"
                                                  },
                                                  {
                                                    "value": "MODERN",
                                                    "description": "모던형"
                                                  }
                                                ],
                                                "careerTypes": [
                                                  {
                                                    "value": "FRESHMAN",
                                                    "description": "신입"
                                                  },
                                                  {
                                                    "value": "EXPERIENCED",
                                                    "description": "경력"
                                                  }
                                                ],
                                                "degreeLevels": [
                                                  {
                                                    "value": "HIGH_SCHOOL",
                                                    "description": "고졸"
                                                  },
                                                  {
                                                    "value": "ASSOCIATE",
                                                    "description": "전문학사"
                                                  },
                                                  {
                                                    "value": "BACHELOR",
                                                    "description": "학사"
                                                  },
                                                  {
                                                    "value": "MASTER",
                                                    "description": "석사"
                                                  },
                                                  {
                                                    "value": "DOCTORATE",
                                                    "description": "박사"
                                                  }
                                                ],
                                                "proficiencyLevels": [
                                                  {
                                                    "value": "BEGINNER",
                                                    "description": "초급"
                                                  },
                                                  {
                                                    "value": "INTERMEDIATE",
                                                    "description": "중급"
                                                  },
                                                  {
                                                    "value": "ADVANCED",
                                                    "description": "고급"
                                                  }
                                                ],
                                                "projectTypes": [
                                                  {
                                                    "value": "PERSONAL",
                                                    "description": "개인"
                                                  },
                                                  {
                                                    "value": "TEAM",
                                                    "description": "팀"
                                                  },
                                                  {
                                                    "value": "COMPANY",
                                                    "description": "회사"
                                                  }
                                                ],
                                                "additionalInfoCategories": [
                                                  {
                                                    "value": "AWARD",
                                                    "description": "수상내역"
                                                  },
                                                  {
                                                    "value": "LANGUAGE",
                                                    "description": "어학능력"
                                                  },
                                                  {
                                                    "value": "CERTIFICATE",
                                                    "description": "자격증"
                                                  },
                                                  {
                                                    "value": "ACTIVITY",
                                                    "description": "대외활동"
                                                  }
                                                ]
                                              }
                                            }
                                            """
                                    )
                            )
                    )
            }
    )
    @GetMapping
    public ResponseEntity<CommonResponse<ResumeMetadataResponse>> getResumeMetadata() {
        MDC.put("spanId", "metadata-controller");

        log.info("이력서 메타데이터 조회 요청");

        ResumeMetadataResponse metadata = resumeMetadataService.getResumeMetadata();

        log.info("메타데이터 조회 완료 - 기술스택: {}개, Enum 카테고리: 6개",
                metadata.techStacks().size());

        return ResponseEntity.ok(CommonResponse.success("이력서 메타데이터를 성공적으로 조회했습니다.", metadata));
    }
}