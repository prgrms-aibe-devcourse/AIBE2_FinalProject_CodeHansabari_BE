package com.cvmento.domain.resume.service;

import com.cvmento.domain.resume.dto.response.SuggestedResumeSectionDto;
import com.cvmento.domain.resume.dto.response.SuggestedResumeItemDto;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ResumeLlmPromptService {

    /**
     * AI 제안용 프롬프트 생성
     */
    public String buildSuggestionPrompt(String experienceContent) {
        return buildPromptStructure() +
                buildExperienceSection(experienceContent) +
                buildRequestSection() +
                buildGuidelines();
    }

    /**
     * 이미지 기반 이력서 import 프롬프트 - 새로운 필드 구조 반영
     */
    public String buildResumeImportPrompt() {
        return """
            당신은 전문 데이터 추출 전문가입니다. 제공된 이력서 이미지를 분석하고 내용을 구조화된 JSON 형식으로 추출하세요.
            출력은 ResumeCreateRequest DTO의 구조를 엄격하게 따르는 유효한 JSON 객체여야 합니다.

            ## JSON 출력 형식:
            
            ```json
            {
              "title": "홍길동의 이력서",
              "memberInfo": {
                "name": "홍길동",
                "age": "27",
                "careerType": "신입",
                "email": "hong@example.com",
                "phoneNumber": "010-1234-5678",
                "blogUrl": "https://blog.honggildong.dev",
                "githubUrl": "https://github.com/honggildong",
                "notionUrl": "https://www.notion.so/honggildong",
                "introduction": "안녕하세요! 신입 백엔드 개발자 홍길동입니다. 컴퓨터공학을 전공하며 웹 개발에 관심을 가지게 되었고, 특히 서버 사이드 개발과 데이터베이스 설계에 열정을 가지고 있습니다.",
                "techStack": ["Java", "Spring Boot", "MySQL", "Docker"]
              },
              "sections": [
                {
                  "sectionType": "EDUCATION",
                  "sectionTitle": "학력",
                  "items": [
                    {
                      "title": "ABC대학교",
                      "subTitle": "컴퓨터공학과",
                      "startDate": "2018-03-01",
                      "endDate": "2022-02-28",
                      "description": "학사 졸업, 전체 학점 4.2/4.5"
                    }
                  ]
                },
                {
                  "sectionType": "WORK_EXPERIENCE",
                  "sectionTitle": "경력",
                  "items": [
                    {
                      "title": "XYZ소프트웨어",
                      "subTitle": "백엔드 개발 인턴",
                      "startDate": "2023-01-01",
                      "endDate": "2023-12-31",
                      "description": "Spring Boot를 이용한 RESTful API 개발 및 배포 참여"
                    }
                  ]
                }
              ]
            }
            ```

            ### 중요 지침:
            1. **memberInfo 필드**: 모든 개인정보는 memberInfo 안에 포함
               - name, age, careerType, email, phoneNumber, blogUrl, githubUrl, notionUrl, introduction, techStack
            2. **careerType**: "신입", "경력", "인턴" 등으로 분류
            3. **introduction**: 자기소개 내용 (기존의 selfIntroduction)
            4. **techStack**: 기술 스택을 배열로 추출
            5. **섹션 타입**: EDUCATION, WORK_EXPERIENCE, PROJECT, SKILL, CERTIFICATE, LANGUAGE, AWARD 중 선택
            6. **날짜 형식**: YYYY-MM-DD 형식, 정보가 없으면 null
            7. **필드 분리**: title(기관명), subTitle(직책/전공), description(상세 내용)은 각각 분리
            8. **유효한 JSON**: 마크다운이나 추가 설명 없이 순수 JSON만 출력
            """;
    }

    /**
     * OCR 텍스트 기반 이력서 import 프롬프트 - 새로운 필드 구조 반영
     */
    public String buildResumeTextImportPrompt(String ocrText) {
        return """
            당신은 전문 데이터 추출 전문가입니다. OCR로 추출된 다음 이력서 텍스트를 분석하고 내용을 구조화된 JSON 형식으로 변환하세요.
            출력은 ResumeCreateRequest DTO의 구조를 엄격하게 따르는 유효한 JSON 객체여야 합니다.

            ## 분석할 OCR 추출 텍스트:
            ```text
            %s
            ```

            ## JSON 출력 형식:
            
            ```json
            {
              "title": "홍길동의 이력서",
              "memberInfo": {
                "name": "홍길동",
                "age": "27",
                "careerType": "신입",
                "email": "hong@example.com",
                "phoneNumber": "010-1234-5678",
                "blogUrl": "https://blog.honggildong.dev",
                "githubUrl": "https://github.com/honggildong",
                "notionUrl": "https://www.notion.so/honggildong",
                "introduction": "안녕하세요! 신입 백엔드 개발자 홍길동입니다. 컴퓨터공학을 전공하며 웹 개발에 관심을 가지게 되었고, 특히 서버 사이드 개발과 데이터베이스 설계에 열정을 가지고 있습니다.",
                "techStack": ["Java", "Spring Boot", "MySQL", "Docker"]
              },
              "sections": [
                {
                  "sectionType": "EDUCATION",
                  "sectionTitle": "학력",
                  "items": [
                    {
                      "title": "ABC대학교",
                      "subTitle": "컴퓨터공학과",
                      "startDate": "2018-03-01",
                      "endDate": "2022-02-28",
                      "description": "학사 졸업, 전체 학점 4.2/4.5"
                    }
                  ]
                }
              ]
            }
            ```

            ### 중요 지침:
            1. **memberInfo 통합**: 모든 개인정보는 memberInfo 안에 포함
               - name, age, careerType, email, phoneNumber, blogUrl, githubUrl, notionUrl, introduction, techStack
            2. **필드 추출 규칙**:
               - name: 이름 추출
               - age: 나이 또는 생년월일에서 추정
               - careerType: "신입", "경력", "인턴" 등 경력 수준
               - email: 이메일 주소
               - phoneNumber: 전화번호
               - blogUrl, githubUrl, notionUrl: 각각의 URL 추출
               - introduction: 자기소개 내용
               - techStack: 기술 스택을 배열로 추출
            3. **섹션 타입**: EDUCATION, WORK_EXPERIENCE, PROJECT, SKILL, CERTIFICATE, LANGUAGE, AWARD 중 선택
            4. **날짜 형식**: YYYY-MM-DD 형식, 정보가 없으면 null
            5. **필드 분리**: title(기관명), subTitle(직책/전공), description(상세 내용)은 각각 분리
            6. **유효한 JSON**: 마크다운이나 추가 설명 없이 순수 JSON만 출력
            """.formatted(ocrText);
    }

    // ================ Private Helper Methods ================

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