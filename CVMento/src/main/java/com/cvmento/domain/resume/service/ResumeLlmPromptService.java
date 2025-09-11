package com.cvmento.domain.resume.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.Base64;

@Service
@Slf4j
public class ResumeLlmPromptService {

    public VisionPromptResult createVisionPrompt(MultipartFile file) {
        MDC.put("spanId", "resume-vision-prompt-service");
        
        try {
            String base64Image = convertFileToBase64Image(file);
            String textPrompt = buildVisionTextPrompt();
            
            log.info("이력서 Vision 프롬프트 생성 완료 - 파일크기: {}bytes, Base64길이: {}chars",
                    file.getSize(), base64Image.length());
            
            return new VisionPromptResult(textPrompt, base64Image);
            
        } catch (Exception e) {
            log.error("Vision 프롬프트 생성 중 오류: {}", e.getMessage(), e);
            throw new RuntimeException("Vision 프롬프트 생성 실패", e);
        }
    }
    
    // 기존 메서드도 유지 (텍스트 기반 처리용)
    public String createResumeConversionPrompt(MultipartFile file) {
        MDC.put("spanId", "resume-prompt-service");
        
        try {
            String fileInfo = extractFileInfo(file);
            String prompt = buildPromptWithFileInfo(fileInfo);
            
            log.info("이력서 변환 프롬프트 생성 완료 - 파일크기: {}bytes, 프롬프트길이: {}",
                    file.getSize(), prompt.length());
            
            return prompt;
            
        } catch (Exception e) {
            log.error("프롬프트 생성 중 오류: {}", e.getMessage(), e);
            throw new RuntimeException("프롬프트 생성 실패", e);
        }
    }

    private String convertFileToBase64Image(MultipartFile file) {
        try {
            String contentType = file.getContentType();
            if (contentType == null) {
                throw new RuntimeException("파일 타입을 확인할 수 없습니다.");
            }

            if (contentType.contains("image")) {
                // 이미지 파일인 경우 바로 Base64 변환
                return convertImageToBase64(file);
            } else if (contentType.contains("pdf")) {
                // PDF 파일인 경우 이미지로 변환 후 Base64 변환
                return convertPdfToBase64Image(file);
            } else {
                throw new RuntimeException("지원하지 않는 파일 형식입니다. 이미지 또는 PDF 파일만 지원됩니다.");
            }

        } catch (Exception e) {
            log.error("파일을 Base64 이미지로 변환 실패: {}", e.getMessage(), e);
            throw new RuntimeException("파일 변환 실패", e);
        }
    }

    private String convertImageToBase64(MultipartFile file) {
        try {
            byte[] imageBytes = file.getBytes();
            String base64 = Base64.getEncoder().encodeToString(imageBytes);
            String contentType = file.getContentType();
            
            log.info("이미지 파일 Base64 변환 완료 - 타입: {}, 크기: {}bytes", 
                    contentType, imageBytes.length);
            
            return "data:" + contentType + ";base64," + base64;
            
        } catch (Exception e) {
            log.error("이미지 Base64 변환 실패: {}", e.getMessage());
            throw new RuntimeException("이미지 변환 실패", e);
        }
    }

    private String convertPdfToBase64Image(MultipartFile file) {
        try {
            byte[] pdfBytes = file.getBytes();
            
            try (PDDocument document = Loader.loadPDF(pdfBytes)) {
                if (document.getNumberOfPages() == 0) {
                    throw new RuntimeException("PDF 파일에 페이지가 없습니다.");
                }
                
                // 첫 번째 페이지만 변환
                PDFRenderer pdfRenderer = new PDFRenderer(document);
                BufferedImage bufferedImage = pdfRenderer.renderImageWithDPI(0, 150);
                
                // BufferedImage를 JPEG 바이트 배열로 변환
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(bufferedImage, "jpeg", baos);
                byte[] imageBytes = baos.toByteArray();
                
                String base64 = Base64.getEncoder().encodeToString(imageBytes);
                
                log.info("PDF를 이미지로 변환 후 Base64 변환 완료 - 크기: {}bytes", imageBytes.length);
                
                return "data:image/jpeg;base64," + base64;
                
            }
        } catch (Exception e) {
            log.error("PDF Base64 변환 실패: {}", e.getMessage(), e);
            throw new RuntimeException("PDF 변환 실패", e);
        }
    }

    private String buildVisionTextPrompt() {
        StringBuilder prompt = new StringBuilder();
        
        prompt.append("당신은 이력서 변환 전문가입니다. ");
        prompt.append("업로드된 이력서 이미지에서 정보를 정확히 추출하여 JSON 형식으로 변환해주세요.\n\n");
        
        prompt.append("위 이력서 이미지를 분석하여 다음 JSON 형식으로 정확히 변환해주세요:\n");
        prompt.append(getJsonStructure());
        
        return prompt.toString();
    }

    private String extractFileInfo(MultipartFile file) {
        try {
            String fileName = file.getOriginalFilename() != null ? file.getOriginalFilename() : "unknown";
            String contentType = file.getContentType() != null ? file.getContentType() : "unknown";
            long fileSize = file.getSize();
            
            // 파일이 텍스트 형식이면 내용 일부 읽기 시도
            String fileContent = "";
            if (contentType.contains("text") || fileName.toLowerCase().endsWith(".txt")) {
                try {
                    byte[] bytes = file.getBytes();
                    fileContent = new String(bytes, "UTF-8");
                    if (fileContent.length() > 1000) {
                        fileContent = fileContent.substring(0, 1000) + "...";
                    }
                } catch (Exception e) {
                    log.warn("텍스트 파일 읽기 실패: {}", e.getMessage());
                }
            }
            
            return String.format("파일명: %s, 타입: %s, 크기: %d bytes%s", 
                    fileName, contentType, fileSize,
                    fileContent.isEmpty() ? "" : "\n파일 내용:\n" + fileContent);
                    
        } catch (Exception e) {
            log.error("파일 정보 추출 실패: {}", e.getMessage());
            return "파일 정보를 읽을 수 없습니다.";
        }
    }

    private String buildPromptWithFileInfo(String fileInfo) {
        return String.format(
                "다음 파일 정보를 바탕으로 이력서 JSON을 생성해주세요:\n\n" +
                "%s\n\n" +
                "아래 JSON 형식으로 반환해주세요 (반드시 유효한 JSON만):\n\n" +
                "{\n" +
                "  \"title\": \"백엔드 개발자 이력서\",\n" +
                "  \"type\": \"DEFAULT\",\n" +
                "  \"name\": \"김개발\",\n" +
                "  \"email\": \"kim@example.com\",\n" +
                "  \"birthYear\": 1995,\n" +
                "  \"phone\": \"010-1234-5678\",\n" +
                "  \"careerType\": \"FRESHMAN\",\n" +
                "  \"fieldName\": \"백엔드 개발자\",\n" +
                "  \"introduction\": \"열정적인 개발자입니다.\",\n" +
                "  \"githubUrl\": null,\n" +
                "  \"blogUrl\": null,\n" +
                "  \"notionUrl\": null,\n" +
                "  \"educations\": [],\n" +
                "  \"techStacks\": [],\n" +
                "  \"customLinks\": [],\n" +
                "  \"careers\": [],\n" +
                "  \"projects\": [],\n" +
                "  \"trainings\": [],\n" +
                "  \"additionalInfos\": []\n" +
                "}\n\n" +
                "careerType: FRESHMAN 또는 EXPERIENCED만 가능합니다.",
                fileInfo
        );
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
        return "업로드된 파일을 분석하여 다음 JSON 형식으로 변환해주세요. 반드시 유효한 JSON만 반환하세요:\n\n" +
               "{\n" +
               "  \"title\": \"이력서 제목\",\n" +
               "  \"type\": \"DEFAULT\",\n" +
               "  \"name\": \"이름\",\n" +
               "  \"email\": \"이메일\",\n" +
               "  \"birthYear\": 1990,\n" +
               "  \"phone\": \"전화번호\",\n" +
               "  \"careerType\": \"FRESHMAN\",\n" +
               "  \"fieldName\": \"직무분야\",\n" +
               "  \"introduction\": \"자기소개\",\n" +
               "  \"githubUrl\": null,\n" +
               "  \"blogUrl\": null,\n" +
               "  \"notionUrl\": null,\n" +
               "  \"educations\": [],\n" +
               "  \"techStacks\": [],\n" +
               "  \"customLinks\": [],\n" +
               "  \"careers\": [],\n" +
               "  \"projects\": [],\n" +
               "  \"trainings\": [],\n" +
               "  \"additionalInfos\": []\n" +
               "}\n\n" +
               "careerType은 FRESHMAN 또는 EXPERIENCED 중 하나입니다.";
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