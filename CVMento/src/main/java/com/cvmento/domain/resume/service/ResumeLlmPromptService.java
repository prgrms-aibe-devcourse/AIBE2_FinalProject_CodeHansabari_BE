package com.cvmento.domain.resume.service;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Base64;

@Service
@Slf4j
public class ResumeLlmPromptService {

    public String createResumeConversionPrompt(MultipartFile file) {
        MDC.put("spanId", "resume-prompt-service");
        
        try {
            String prompt = buildSimplePrompt();
            
            log.info("이력서 변환 프롬프트 생성 완료 - 파일크기: {}bytes, 프롬프트길이: {}",
                    file.getSize(), prompt.length());
            
            return prompt;
            
        } catch (Exception e) {
            log.error("프롬프트 생성 중 오류: {}", e.getMessage(), e);
            throw new RuntimeException("프롬프트 생성 실패", e);
        }
    }



    public String createResumeConversionPrompt(String extractedText) {
        MDC.put("spanId", "resume-prompt-service");
        
        String prompt = buildPromptForText(extractedText);
        
        log.info("텍스트 기반 이력서 변환 프롬프트 생성 완료 - 텍스트길이: {}, 프롬프트길이: {}",
                extractedText.length(), prompt.length());
        
        return prompt;
    }

    private String determineFileType(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType == null) {
            return "unknown";
        }
        
        if (contentType.contains("pdf")) {
            return "pdf";
        } else if (contentType.contains("image")) {
            return "image";
        } else {
            return "unknown";
        }
    }

    private String buildSimplePrompt() {
        StringBuilder prompt = new StringBuilder();
        
        prompt.append("당신은 이력서 변환 전문가입니다. ");
        prompt.append("업로드된 파일을 분석하여 아래 JSON 형식으로 정확히 변환해주세요.\n\n");
        prompt.append("중요: 반드시 유효한 JSON만 반환하고, 다른 설명은 포함하지 마세요.\n\n");
        prompt.append(getJsonStructure());
        
        return prompt.toString();
    }


    private String buildPromptForText(String extractedText) {
        StringBuilder prompt = new StringBuilder();
        
        prompt.append("당신은 이력서 변환 전문가입니다. ");
        prompt.append("다음 추출된 이력서 텍스트를 분석하여 JSON 형식으로 변환해주세요.\n\n");
        prompt.append("=== 추출된 이력서 텍스트 ===\n");
        prompt.append(extractedText);
        prompt.append("\n\n=== 변환할 JSON 형식 ===\n");
        prompt.append(getJsonStructure());
        
        return prompt.toString();
    }

    private String getJsonStructure() {
        return """
        {
          "title": "이력서 제목 (예: 백엔드 개발자 김철수)",
          "type": "DEFAULT", // DEFAULT, MODERN, CREATIVE 중 하나
          "name": "성명",
          "email": "이메일 주소",
          "birthYear": 출생년도(숫자),
          "phone": "전화번호",
          "careerType": "FRESHMAN", // FRESHMAN(신입), EXPERIENCED(경력) 중 하나
          "fieldName": "분야/직무명",
          "introduction": "자기소개 (없으면 null)",
          "githubUrl": "GitHub URL (없으면 null)",
          "blogUrl": "블로그 URL (없으면 null)",
          "notionUrl": "Notion URL (없으면 null)",
          "educations": [
            {
              "schoolName": "학교명",
              "major": "전공",
              "degreeLevel": "BACHELOR", // HIGH_SCHOOL, ASSOCIATE, BACHELOR, MASTER, DOCTORATE
              "personalGpa": 개인성적(소수점),
              "totalGpa": 만점성적(소수점),
              "graduationDate": "졸업일(YYYY-MM-DD)"
            }
          ],
          "techStacks": [
            {
              "techStackId": 1, // 임시로 1 설정
              "proficiencyLevel": "INTERMEDIATE" // BEGINNER, INTERMEDIATE, ADVANCED, EXPERT
            }
          ],
          "customLinks": [
            {
              "linkName": "링크명",
              "linkUrl": "URL"
            }
          ],
          "careers": [
            {
              "startDate": "시작일(YYYY-MM-DD)",
              "endDate": "종료일(YYYY-MM-DD)",
              "companyName": "회사명",
              "companyDescription": "회사 설명",
              "departmentPosition": "부서/직책",
              "mainTasks": "주요 업무",
              "techStacks": [
                {
                  "techStackId": 1,
                  "proficiencyLevel": "INTERMEDIATE"
                }
              ]
            }
          ],
          "projects": [
            {
              "projectName": "프로젝트명",
              "projectType": "PERSONAL", // TEAM, PERSONAL, COMPANY
              "startDate": "시작일(YYYY-MM-DD)",
              "endDate": "종료일(YYYY-MM-DD)",
              "description": "프로젝트 설명",
              "projectUrl": "프로젝트 URL (없으면 null)",
              "githubUrl": "GitHub URL (없으면 null)",
              "techStacks": [
                {
                  "techStackId": 1,
                  "proficiencyLevel": "INTERMEDIATE"
                }
              ]
            }
          ],
          "trainings": [
            {
              "trainingName": "교육/훈련명",
              "organization": "교육기관",
              "startDate": "시작일(YYYY-MM-DD)",
              "endDate": "종료일(YYYY-MM-DD)",
              "description": "교육 내용",
              "certificateUrl": "수료증 URL (없으면 null)",
              "techStacks": [
                {
                  "techStackId": 1,
                  "proficiencyLevel": "INTERMEDIATE"
                }
              ]
            }
          ],
          "additionalInfos": [
            {
              "category": "AWARD", // AWARD, CERTIFICATE, LANGUAGE, ETC
              "title": "제목",
              "description": "상세 설명",
              "achievementDate": "취득/수상일(YYYY-MM-DD)"
            }
          ]
        }
        
        중요 사항:
        1. 정보가 없는 필드는 빈 배열([]) 또는 null로 설정
        2. techStackId는 임시로 1로 설정 (실제 구현에서 매핑 필요)
        3. 날짜는 반드시 YYYY-MM-DD 형식으로 변환
        4. 경력이 있으면 EXPERIENCED, 신입이면 FRESHMAN으로 설정
        5. 반드시 유효한 JSON 형식으로 응답
        6. 마크다운 코드블록(```) 사용하지 말고 순수 JSON만 반환
        7. 응답은 { 로 시작해서 } 로 끝나야 함
        8. 추가 설명이나 텍스트 없이 JSON만 반환
        """;
    }
}