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
