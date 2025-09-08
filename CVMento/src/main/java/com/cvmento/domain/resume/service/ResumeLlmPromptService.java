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
            You are an expert data extraction specialist. Analyze the provided resume image and extract its content into a structured JSON format.
            The output MUST be a valid JSON object that strictly follows the structure of the `ResumeCreateRequest` DTO.

            ## JSON Output Format:
            The JSON must match this structure exactly. Do not add any extra fields or explanations.

            ```json
            {
              "title": "Extracted Resume Title (e.g., 'John Doe's Resume')",
              "memberInfo": {
                "name": "Full Name",
                "email": "email@example.com",
                "phoneNumber": "010-1234-5678",
                "blogUrl": "https://example.blog"
              },
              "sections": [
                {
                  "sectionType": "WORK_EXPERIENCE",
                  "sectionTitle": "Work Experience",
                  "items": [
                    {
                      "title": "Company Name",
                      "subTitle": "Job Title",
                      "startDate": "YYYY-MM-DD",
                      "endDate": "YYYY-MM-DD",
                      "description": "Details of responsibilities and achievements."
                    }
                  ]
                },
                {
                  "sectionType": "EDUCATION",
                  "sectionTitle": "Education",
                  "items": [
                    {
                      "title": "University Name",
                      "subTitle": "Degree and Major",
                      "startDate": "YYYY-MM-DD",
                      "endDate": "YYYY-MM-DD",
                      "description": "GPA or other details."
                    }
                  ]
                }
              ]
            }
            ```

            ### Important Instructions:
            1.  **Extract All Sections**: Identify and extract all relevant sections like "Work Experience", "Education", "Projects", "Skills", "Certificates", etc.
            2.  **Use Correct `sectionType`**: For each section, use the most appropriate `sectionType` from this list: `EDUCATION`, `WORK_EXPERIENCE`, `PROJECT`, `SKILL`, `CERTIFICATE`, `LANGUAGE`, `AWARD`.
            3.  **Date Format**: All dates (`startDate`, `endDate`) must be in `YYYY-MM-DD` format. If a year and month are present but no day, use the first day of the month (e.g., '2023-05-01'). If a date is not present, use `null`. If a date is not present, use `null`.
            4.  **Title**: Create a suitable title for the resume, like "[Name]'s Resume".
            5.  **Valid JSON**: The final output must be ONLY the JSON object, without any surrounding text, explanations, or markdown formatting like ```json.
            """;
    }

    public String buildResumeTextImportPrompt(String ocrText) {
        return """
            You are an expert data extraction specialist. Analyze the provided resume text, which was extracted via OCR, and convert its content into a structured JSON format.
            The output MUST be a valid JSON object that strictly follows the structure of the `ResumeCreateRequest` DTO.

            ## OCR-Extracted Text to Analyze:
            ```text
            %s
            ```

            ## JSON Output Format:
            The JSON must match this structure exactly. Do not add any extra fields or explanations.

            ```json
            {
              "title": "Extracted Resume Title (e.g., 'John Doe's Resume')",
              "memberInfo": {
                "name": "Full Name",
                "email": "email@example.com",
                "phoneNumber": "010-1234-5678",
                "blogUrl": "https://example.blog"
              },
              "sections": [
                {
                  "sectionType": "WORK_EXPERIENCE",
                  "sectionTitle": "Work Experience",
                  "items": [
                    {
                      "title": "Company Name",
                      "subTitle": "Job Title",
                      "startDate": "YYYY-MM-DD",
                      "endDate": "YYYY-MM-DD",
                      "description": "Details of responsibilities and achievements."
                    }
                  ]
                },
                {
                  "sectionType": "EDUCATION",
                  "sectionTitle": "Education",
                  "items": [
                    {
                      "title": "University Name",
                      "subTitle": "Degree and Major",
                      "startDate": "YYYY-MM-DD",
                      "endDate": "YYYY-MM-DD",
                      "description": "GPA or other details."
                    }
                  ]
                }
              ]
            }
            ```

            ### Important Instructions:
            1.  **Extract All Sections**: From the provided text, identify and extract all relevant sections like "Work Experience", "Education", "Projects", "Skills", "Certificates", etc.
            2.  **Use Correct `sectionType`**: For each section, use the most appropriate `sectionType` from this list: `EDUCATION`, `WORK_EXPERIENCE`, `PROJECT`, `SKILL`, `CERTIFICATE`, `LANGUAGE`, `AWARD`.
            3.  **Date Format**: All dates (`startDate`, `endDate`) must be in `YYYY-MM-DD` format. If a year and month are present but no day, use the first day of the month (e.g., '2023-05-01'). If a date is not present, use `null`.
            4.  **Title**: Create a suitable title for the resume, like "[Name]'s Resume".
            5.  **Valid JSON**: The final output must be ONLY the JSON object, without any surrounding text, explanations, or markdown formatting like ```json.
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