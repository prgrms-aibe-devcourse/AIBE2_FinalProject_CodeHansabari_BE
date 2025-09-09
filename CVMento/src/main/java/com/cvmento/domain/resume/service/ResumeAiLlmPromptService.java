package com.cvmento.domain.resume.service;

import com.cvmento.domain.member.entity.Member;
import com.cvmento.domain.resume.dto.request.UserExperienceRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ResumeAiLlmPromptService {

    // ======================== Public Methods ========================

    /**
     * 사용자 경험 기반 이력서 섹션 추가 제안 프롬프트 생성
     */
    public String buildFullResumeSuggestionPrompt(UserExperienceRequest request, Member member) {
        return buildPromptStructure() +
                buildUserInfoSection(request, member) +
                buildSectionAdditionRequestSection() +
                buildResumeJsonFormat() +
                buildCompleteGuidelines();
    }

    // ======================== Common Sections ========================

    private String buildPromptStructure() {
        return """
            이력서 컨설턴트로서 사용자 경험을 바탕으로 이력서 섹션을 제안해주세요.
            반드시 아래 JSON 형식으로만 응답하세요.
            
            """;
    }

    private String buildUserInfoSection(UserExperienceRequest request, Member member) {
        StringBuilder section = new StringBuilder("## 사용자 정보\\n");
        section.append("**이름**: ").append(member.getName()).append("\\n");
        section.append("**이메일**: ").append(member.getEmail()).append("\\n");
        section.append("**경력구분**: ").append(request.careerType()).append("\\n");
        section.append("**지원분야**: ").append(request.fieldName()).append("\\n\\n");
        
        section.append("**경험 및 경력사항**:\\n").append(request.experiences()).append("\\n\\n");
        
        if (request.techStacks() != null && !request.techStacks().isEmpty()) {
            section.append("**보유 기술스택**: ").append(String.join(", ", request.techStacks())).append("\\n\\n");
        }
        
        if (request.additionalInfo() != null && !request.additionalInfo().trim().isEmpty()) {
            section.append("**추가 정보**: ").append(request.additionalInfo()).append("\\n\\n");
        }
        
        return section.toString();
    }

    // ======================== Request Sections ========================

    private String buildSectionAdditionRequestSection() {
        return """
            ## 작업 요청
            위 사용자의 경험 정보를 바탕으로 이력서에 추가할 수 있는 섹션 내용들을 제안해주세요.
            
            **제안 원칙:**
            - 사용자가 실제 경험한 내용만을 바탕으로 제안
            - 과장하거나 허위 내용 생성 금지
            - 경험에서 추출할 수 있는 현실적인 내용만 제안
            - 빈 섹션이 있어도 괜찮음 (경험이 없다면 비워둘 것)
            
            **제안 범위:**
            - 경력 정보: 회사명, 기간, 직책, 주요 업무와 성과
            - 프로젝트: 관련 프로젝트 내용
            - 기술스택: 사용 기술과 숙련도
            - 학력 정보: 추정 가능한 범위에서만
            - 교육이력: 언급된 교육이나 자격증
            - 기타사항: 자격증, 수상 등 (언급된 경우에만)
            
            """;
    }

    // ======================== JSON Format Sections ========================

    private String buildResumeJsonFormat() {
        return """
            ## 필수 응답 형식 - 절대 무시하지 마세요!
            
            오직 아래 JSON 형식으로만 응답하세요. 다른 텍스트, 설명, 주석은 절대 포함하지 마세요.
            모든 필드는 반드시 포함되어야 하고, null이나 빈 값을 허용하지 않습니다.
            
            ```json
            {
              "suggestedResume": {
                "title": "백엔드 개발자 이력서",
                "type": "DEFAULT", 
                "name": "홍길동",
                "email": "user@example.com",
                "birthYear": null,
                "phone": "010-1234-5678",
                "careerType": "EXPERIENCED",
                "fieldName": "백엔드 개발자",
                "introduction": "2년간의 백엔드 개발 경험을 바탕으로 효율적인 API 설계와 데이터베이스 최적화 역량을 보유하고 있습니다.",
                "githubUrl": null,
                "blogUrl": null,
                "notionUrl": null,
                "educations": [
                  {
                    "schoolName": "학교명 (추정 가능한 경우에만)",
                    "major": "전공명 (추정 가능한 경우에만)",
                    "degreeLevel": "BACHELOR",
                    "personalGpa": null,
                    "totalGpa": null,
                    "graduationDate": null
                  }
                ],
                "techStacks": [
                  {
                    "techStackId": 1,
                    "techStackName": "Java",
                    "category": "Programming Language",
                    "proficiencyLevel": "INTERMEDIATE",
                    "experienceDescription": "경험 설명"
                  }
                ],
                "careers": [
                  {
                    "startDate": "2022-01-01",
                    "endDate": "2023-12-31",
                    "companyName": "회사명",
                    "companyDescription": "회사 설명",
                    "departmentPosition": "부서/직책",
                    "mainTasks": "주요 업무 및 성과",
                    "techStacks": [
                      {
                        "techStackId": 1,
                        "techStackName": "Java",
                        "proficiencyLevel": "INTERMEDIATE"
                      }
                    ]
                  }
                ],
                "projects": [
                  {
                    "startDate": "2023-03-01",
                    "endDate": "2023-08-31",
                    "name": "프로젝트명",
                    "description": "프로젝트 소개",
                    "detailedDescription": "상세 설명",
                    "repositoryUrl": null,
                    "deployUrl": null,
                    "projectType": "PERSONAL",
                    "techStacks": [
                      {
                        "techStackId": 1,
                        "techStackName": "React",
                        "proficiencyLevel": "ADVANCED"
                      }
                    ]
                  }
                ],
                "trainings": [],
                "additionalInfos": []
              },
              "improvementTips": [
                "작성 팁 1",
                "작성 팁 2"
              ],
              "missingElements": [
                "부족한 요소 1",
                "부족한 요소 2"
              ]
            }
            ```
            
            """;
    }

    // ======================== Guideline Components ========================

    private String buildCompleteGuidelines() {
        return """
            ### 🚨 절대 엄수 규칙 🚨
            
            1. **오직 JSON만**: 위의 JSON 형식 외에는 어떤 텍스트도 출력하지 마세요
            2. **모든 키 필수**: suggestedResume, improvementTips, missingElements는 절대 빠뜨리지 마세요  
            3. **빈 값 금지**: title, name, email, careerType, fieldName, introduction은 반드시 채우세요
            4. **형식 준수**: JSON 문법을 정확히 따르고 쉼표, 중괄호를 빠뜨리지 마세요
            
            **이력서 섹션 추가 제안 원칙:**
            1. **사실 기반**: 사용자가 제공한 정보만을 바탕으로 제안
            2. **현실적 수준**: 경력과 경험에 맞는 적절한 수준으로 제안
            3. **구체성**: 모호한 표현보다 구체적이고 명확한 내용
            4. **성과 중심**: 업무 설명보다 성과와 결과를 강조
            5. **기술 연관성**: 언급된 기술스택과 일치하는 내용으로 구성
            6. **빈 값 허용**: 경험이 없는 부분은 빈 배열 또는 null로 설정
            
            **작성 스타일:**
            - 전문적이면서도 읽기 쉬운 문체
            - 능동적이고 적극적인 표현 사용
            - 숫자와 데이터를 활용한 구체적 서술
            - 과장이나 허위 내용 절대 금지
            
            **기술스택 숙련도 기준:**
            - BEGINNER: 기본 문법, 간단한 활용 (6개월 미만)
            - INTERMEDIATE: 실무 활용, 프로젝트 경험 (6개월~2년)
            - ADVANCED: 전문 활용, 최적화, 문제해결 (2년 이상)
            
            **빈 값 처리:**
            - 정보가 없는 필드: null 또는 "-" 사용
            - 빈 배열: [] 사용
            - 추정할 수 없는 날짜: null 사용
            - 확실하지 않은 정보: 포함하지 않음
            
            """;
    }
}