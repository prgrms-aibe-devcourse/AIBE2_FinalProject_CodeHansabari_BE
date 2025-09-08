package com.cvmento.domain.resume.service;

import com.cvmento.domain.resume.dto.response.SuggestedResumeSectionDto;
import com.cvmento.domain.resume.dto.response.SuggestedResumeItemDto;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ResumeLlmPromptService {

    public String buildSuggestionPrompt(String experienceContent) {
        return buildPromptStructure() +
                buildExperienceSection(experienceContent) +
                buildRequestSection() +
                buildGuidelines();
    }

    public String buildResumeImportPrompt() {
        return """
            당신은 전문 데이터 추출 전문가입니다. 제공된 이력서 이미지를 분석하고 내용을 구조화된 JSON 형식으로 추출하세요.
            출력은 `ResumeCreateRequest` DTO의 구조를 엄격하게 따르는 유효한 JSON 객체여야 합니다.

            ## JSON 출력 형식:
            JSON은 이 구조와 정확히 일치해야 합니다. 추가 필드나 설명을 추가하지 마세요.

            ```json
            {
              "title": "추출된 이력서 제목 (예: '홍길동의 이력서')",
              "memberInfo": {
                "name": "전체 이름",
                "email": "email@example.com",
                "phoneNumber": "010-1234-5678",
                "blogUrl": "https://example.blog"
              },
              "intro": {
                "selfIntroduction": "여기에 자기소개 내용을 입력하세요.",
                "techStack": ["Java", "Spring Boot", "AWS"]
              },
              "sections": [
                {
                  "sectionType": "WORK_EXPERIENCE",
                  "sectionTitle": "경력",
                  "items": [
                    {
                      "title": "회사명",
                      "subTitle": "직책",
                      "startDate": "YYYY-MM-DD",
                      "endDate": "YYYY-MM-DD",
                      "description": "담당 업무 및 성과 상세 내용."
                    }
                  ]
                },
                {
                  "sectionType": "EDUCATION",
                  "sectionTitle": "학력",
                  "items": [
                    {
                      "title": "대학교명",
                      "subTitle": "학위 및 전공",
                      "startDate": "YYYY-MM-DD",
                      "endDate": "YYYY-MM-DD",
                      "description": "학점 또는 기타 상세 내용."
                    }
                  ]
                }
              ]
            }
            ```

            ### 중요 지침:
            1.  **모든 섹션 추출**: "경력", "학력", "프로젝트", "기술", "자격증" 등 모든 관련 섹션을 식별하고 추출하세요.
            2.  **올바른 `sectionType` 사용**: 각 섹션에 대해 다음 목록에서 가장 적절한 `sectionType`을 사용하세요: `EDUCATION`, `WORK_EXPERIENCE`, `PROJECT`, `SKILL`, `CERTIFICATE`, `LANGUAGE`, `AWARD`.
            3.  **날짜 형식**: 모든 날짜(`startDate`, `endDate`)는 `YYYY-MM-DD` 형식이어야 합니다. 연도와 월은 있지만 일은 없는 경우, 해당 월의 첫째 날을 사용하세요(예: '2023-05-01'). 날짜가 없는 경우 `null`을 사용하세요.
            4.  **제목**: "[이름]의 이력서"와 같이 이력서에 적합한 제목을 생성하세요.
            5.  **유효한 JSON**: 최종 출력은 ```json과 같은 주변 텍스트, 설명 또는 마크다운 서식 없이 오직 JSON 객체여야 합니다.
            """;
    }

    public String buildResumeTextImportPrompt(String ocrText) {
        return """
            당신은 전문 데이터 추출 전문가입니다. OCR로 추출된 다음 이력서 텍스트를 분석하고 내용을 구조화된 JSON 형식으로 변환하세요.
            출력은 `ResumeCreateRequest` DTO의 구조를 엄격하게 따르는 유효한 JSON 객체여야 합니다.

            ## 분석할 OCR 추출 텍스트:
            ```text
            %s
            ```

            ## JSON 출력 형식:
            JSON은 이 구조와 정확히 일치해야 합니다. 추가 필드나 설명을 추가하지 마세요.

            ```json
            {
              "title": "추출된 이력서 제목 (예: '홍길동의 이력서')",
              "memberInfo": {
                "name": "전체 이름",
                "email": "email@example.com",
                "phoneNumber": "010-1234-5678",
                "blogUrl": "https://example.blog"
              },
              "intro": {
                "selfIntroduction": "여기에 자기소개 내용을 입력하세요.",
                "techStack": ["Java", "Spring Boot", "AWS"]
              },
              "sections": [
                {
                  "sectionType": "WORK_EXPERIENCE",
                  "sectionTitle": "경력",
                  "items": [
                    {
                      "title": "회사명",
                      "subTitle": "직책",
                      "startDate": "YYYY-MM-DD",
                      "endDate": "YYYY-MM-DD",
                      "description": "담당 업무 및 성과 상세 내용."
                    }
                  ]
                },
                {
                  "sectionType": "EDUCATION",
                  "sectionTitle": "학력",
                  "items": [
                    {
                      "title": "대학교명",
                      "subTitle": "학위 및 전공",
                      "startDate": "YYYY-MM-DD",
                      "endDate": "YYYY-MM-DD",
                      "description": "학점 또는 기타 상세 내용."
                    }
                  ]
                }
              ]
            }
            ```

            ### 중요 지침:
            1.  **모든 섹션 추출**: 제공된 텍스트에서 "경력", "학력", "프로젝트", "기술", "자격증" 등 모든 관련 섹션을 식별하고 추출하세요.
            2.  **올바른 `sectionType` 사용**: 각 섹션에 대해 다음 목록에서 가장 적절한 `sectionType`을 사용하세요: `EDUCATION`, `WORK_EXPERIENCE`, `PROJECT`, `SKILL`, `CERTIFICATE`, `LANGUAGE`, `AWARD`.
            3.  **날짜 형식**: 모든 날짜(`startDate`, `endDate`)는 `YYYY-MM-DD` 형식이어야 합니다. 연도와 월은 있지만 일은 없는 경우, 해당 월의 첫째 날을 사용하세요(예: '2023-05-01'). 날짜가 없는 경우 `null`을 사용하세요.
            4.  **제목**: "[이름]의 이력서"와 같이 이력서에 적합한 제목을 생성하세요.
            5.  **유효한 JSON**: 최종 출력은 ```json과 같은 주변 텍스트, 설명 또는 마크다운 서식 없이 오직 JSON 객체여야 합니다.
            """.formatted(ocrText);
    }

    private String buildPromptStructure() {
        return """
            당신은 20년 경력의 전문 이력서 컨설턴트입니다.
            주어진 경험 내용을 분석하고 이력서에 추가할 수 있는 항목들을 제안해주세요.
            
            """;
    }

    private String buildExperienceSection(String experienceContent) {
        return "## 분석할 경험 내용\n" + experienceContent + "\n\n";
    }

    private String buildRequestSection() {
        return """
            ## 작업 요청
            경험 내용을 바탕으로 이력서에 추가할 수 있는 항목들을 다음 JSON 형식으로 응답해주세요:
            
            ```json
            {
              "suggestedSections": [
                {
                  "sectionType": "EDUCATION",
                  "sectionTitle": "학력",
                  "items": [
                    {
                      "title": "항목 제목",
                      "subTitle": "항목 부제목",
                      "startDate": "YYYY-MM-DD",
                      "endDate": "YYYY-MM-DD",
                      "description": "항목 상세 설명"
                    }
                  ]
                }
              ]
            }
            ```
            
            """;
    }

    private String buildGuidelines() {
        return """
            ### 중요 지침
            1. **구체적인 항목**: 경험 내용을 바탕으로 이력서에 추가할 수 있는 구체적인 항목들을 제안해주세요.
            2. **JSON 형식 준수**: 반드시 유효한 JSON 형식으로 응답해주세요.
            3. **적절한 sectionType**: ResumeSectionType Enum (EDUCATION, WORK_EXPERIENCE, PROJECT, SKILL, CERTIFICATE, LANGUAGE, AWARD) 중 가장 적절한 sectionType을 사용해주세요.
            4. **날짜 형식**: startDate와 endDate는 YYYY-MM-DD 형식을 준수해주세요.
            """;
    }
}