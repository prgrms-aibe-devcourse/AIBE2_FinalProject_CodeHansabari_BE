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
            
            ⚠️ **중요**: 각 item의 title, subTitle, startDate, endDate, description은 반드시 별도로 추출하세요. 
            절대로 "Title: 회사명, SubTitle: 직책, Period: 날짜" 같은 형식으로 description에 모든 정보를 몰아넣지 마세요.

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
                      "title": "네이버",
                      "subTitle": "백엔드 개발자",
                      "startDate": "2022-03-01",
                      "endDate": "2024-02-28",
                      "description": "Spring Boot 기반 REST API 개발 및 데이터베이스 최적화를 담당했습니다. 시스템 성능을 20% 개선하고 CI/CD 파이프라인을 구축했습니다."
                    }
                  ]
                },
                {
                  "sectionType": "EDUCATION",
                  "sectionTitle": "학력",
                  "items": [
                    {
                      "title": "서울대학교",
                      "subTitle": "컴퓨터공학과 학사",
                      "startDate": "2018-03-01",
                      "endDate": "2022-02-28",
                      "description": "전체 학점 4.2/4.5, 졸업논문: 딥러닝 기반 자연어 처리 연구"
                    }
                  ]
                }
              ]
            }
            ```

            ### 중요 지침:
            1.  **모든 섹션 추출**: "경력", "학력", "프로젝트", "기술", "자격증" 등 모든 관련 섹션을 식별하고 추출하세요.
            2.  **올바른 `sectionType` 사용**: 각 섹션에 대해 다음 목록에서 가장 적절한 `sectionType`을 사용하세요: `EDUCATION`, `WORK_EXPERIENCE`, `PROJECT`, `SKILL`, `CERTIFICATE`, `LANGUAGE`, `AWARD`.
            3.  **개별 필드 분리 (중요!)**: 각 item의 필드는 반드시 개별적으로 분리하여 추출하세요:
               - `title`: 기관명, 회사명, 프로젝트명만 (예: "ABC대학교", "삼성전자", "쇼핑몰 프로젝트"). 정보가 없으면 "-"
               - `subTitle`: 세부사항만 (예: "컴퓨터공학과", "소프트웨어 엔지니어", "React 웹 애플리케이션"). 정보가 없으면 "-"  
               - `startDate`/`endDate`: 날짜만 (YYYY-MM-DD 형식). 정보가 없으면 null
               - `description`: 구체적인 설명, 성과, 업무 내용만. 정보가 없으면 "-"
               - **절대 금지**: 모든 정보를 description에 몰아넣기. 각 필드는 고유한 정보만 포함해야 함
            4.  **날짜 형식**: 모든 날짜(`startDate`, `endDate`)는 `YYYY-MM-DD` 형식이어야 합니다. 연도와 월은 있지만 일은 없는 경우, 해당 월의 첫째 날을 사용하세요(예: '2023-05-01'). 날짜가 없는 경우 `null`을 사용하세요.
            5.  **제목**: "[이름]의 이력서"와 같이 이력서에 적합한 제목을 생성하세요.
            6.  **유효한 JSON**: 최종 출력은 ```json과 같은 주변 텍스트, 설명 또는 마크다운 서식 없이 오직 JSON 객체여야 합니다.
            
            ## ❌ 잘못된 예시 (절대 금지):
            ```json
            {
              "title": null,
              "subTitle": null, 
              "startDate": null,
              "endDate": null,
              "description": "Title: ABC대학교, SubTitle: 컴퓨터공학과, Period: 2018-03 - 2022-02, Description: 학사졸업"
            }
            ```
            
            ## ✅ 올바른 예시 (반드시 이렇게):
            ```json
            {
              "title": "ABC대학교",
              "subTitle": "컴퓨터공학과", 
              "startDate": "2018-03-01",
              "endDate": "2022-02-28",
              "description": "학사 졸업"
            }
            ```
            """;
    }

    public String buildResumeTextImportPrompt(String ocrText) {
        return """
            당신은 전문 데이터 추출 전문가입니다. OCR로 추출된 다음 이력서 텍스트를 분석하고 내용을 구조화된 JSON 형식으로 변환하세요.
            출력은 `ResumeCreateRequest` DTO의 구조를 엄격하게 따르는 유효한 JSON 객체여야 합니다.
            
            ⚠️ **중요**: 각 item의 title, subTitle, startDate, endDate, description은 반드시 별도로 추출하세요. 
            절대로 "Title: 회사명, SubTitle: 직책, Period: 날짜" 같은 형식으로 description에 모든 정보를 몰아넣지 마세요.

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
                      "title": "네이버",
                      "subTitle": "백엔드 개발자",
                      "startDate": "2022-03-01",
                      "endDate": "2024-02-28",
                      "description": "Spring Boot 기반 REST API 개발 및 데이터베이스 최적화를 담당했습니다. 시스템 성능을 20% 개선하고 CI/CD 파이프라인을 구축했습니다."
                    }
                  ]
                },
                {
                  "sectionType": "EDUCATION",
                  "sectionTitle": "학력",
                  "items": [
                    {
                      "title": "서울대학교",
                      "subTitle": "컴퓨터공학과 학사",
                      "startDate": "2018-03-01",
                      "endDate": "2022-02-28",
                      "description": "전체 학점 4.2/4.5, 졸업논문: 딥러닝 기반 자연어 처리 연구"
                    }
                  ]
                }
              ]
            }
            ```

            ### 중요 지침:
            1.  **모든 섹션 추출**: 제공된 텍스트에서 "경력", "학력", "프로젝트", "기술", "자격증" 등 모든 관련 섹션을 식별하고 추출하세요.
            2.  **올바른 `sectionType` 사용**: 각 섹션에 대해 다음 목록에서 가장 적절한 `sectionType`을 사용하세요: `EDUCATION`, `WORK_EXPERIENCE`, `PROJECT`, `SKILL`, `CERTIFICATE`, `LANGUAGE`, `AWARD`.
            3.  **개별 필드 분리 (중요!)**: 각 item의 필드는 반드시 개별적으로 분리하여 추출하세요:
               - `title`: 기관명, 회사명, 프로젝트명만 (예: "ABC대학교", "삼성전자", "쇼핑몰 프로젝트"). 정보가 없으면 "-"
               - `subTitle`: 세부사항만 (예: "컴퓨터공학과", "소프트웨어 엔지니어", "React 웹 애플리케이션"). 정보가 없으면 "-"  
               - `startDate`/`endDate`: 날짜만 (YYYY-MM-DD 형식). 정보가 없으면 null
               - `description`: 구체적인 설명, 성과, 업무 내용만. 정보가 없으면 "-"
               - **절대 금지**: 모든 정보를 description에 몰아넣기. 각 필드는 고유한 정보만 포함해야 함
            4.  **날짜 형식**: 모든 날짜(`startDate`, `endDate`)는 `YYYY-MM-DD` 형식이어야 합니다. 연도와 월은 있지만 일은 없는 경우, 해당 월의 첫째 날을 사용하세요(예: '2023-05-01'). 날짜가 없는 경우 `null`을 사용하세요.
            5.  **제목**: "[이름]의 이력서"와 같이 이력서에 적합한 제목을 생성하세요.
            6.  **유효한 JSON**: 최종 출력은 ```json과 같은 주변 텍스트, 설명 또는 마크다운 서식 없이 오직 JSON 객체여야 합니다.
            
            ## ❌ 잘못된 예시 (절대 금지):
            ```json
            {
              "title": null,
              "subTitle": null, 
              "startDate": null,
              "endDate": null,
              "description": "Title: ABC대학교, SubTitle: 컴퓨터공학과, Period: 2018-03 - 2022-02, Description: 학사졸업"
            }
            ```
            
            ## ✅ 올바른 예시 (반드시 이렇게):
            ```json
            {
              "title": "ABC대학교",
              "subTitle": "컴퓨터공학과", 
              "startDate": "2018-03-01",
              "endDate": "2022-02-28",
              "description": "학사 졸업"
            }
            ```
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
            
            **섹션 생성:**
            1. **경험 연관성**: 제안은 제공된 경험 내용과 직접적으로 연관되어야 함
            2. **적절한 분류**: 경험을 가장 적절한 섹션으로 분류 (경력/프로젝트/교육/기술/자격증/언어/수상)
            3. **현실적 수준**: 과장 없이 경험에 기반한 현실적인 내용만 포함
            4. **구체성**: 모호한 표현 대신 구체적이고 측정 가능한 내용 작성
            
            **항목 작성:**
            1. **제목 (title)**: 회사명, 기관명, 프로젝트명 등 핵심 명칭
            2. **부제목 (subTitle)**: 직책, 역할, 학위 등 구체적 역할
            3. **기간 (startDate/endDate)**: YYYY-MM-DD 형식, 정확하지 않은 경우 추정하여 작성
            4. **설명 (description)**: 
               - 구체적인 담당 업무와 성과
               - 사용한 기술 스택 명시
               - 수치화된 결과나 성과 포함
               - 150-300자 분량의 상세한 설명
            
            **섹션 타입 가이드:**
            - WORK_EXPERIENCE: 직장, 인턴, 아르바이트 경험
            - PROJECT: 개인/팀 프로젝트, 포트폴리오
            - EDUCATION: 학력, 교육과정, 부트캠프
            - SKILL: 프로그래밍 언어, 프레임워크, 도구
            - CERTIFICATE: 자격증, 인증서
            - LANGUAGE: 외국어 능력
            - AWARD: 수상 경력, 공모전
            
            **공통 요구사항:**
            - 한국어 작성, 정확한 문법과 맞춤법 사용
            - 과장이나 허위 내용 절대 금지
            - 개인정보(실제 회사명 외) 생성 금지
            - JSON 형식 엄격히 준수
            - 최소 1개, 최대 3개 섹션 생성
            """;
    }
}