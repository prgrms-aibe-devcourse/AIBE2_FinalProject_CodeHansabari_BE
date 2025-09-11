package com.cvmento.domain.resume.service;

import com.cvmento.domain.resume.dto.response.ResumeImportResponse;
import com.cvmento.domain.resume.service.ResumeService;
import com.cvmento.global.aws.LambdaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ResumeImportService {

    private final ResumeLlmPromptService resumeLlmPromptService;
    private final ResumeLlmClientService resumeLlmClientService;
    private final LambdaService lambdaService;
    private final ResumeService resumeService;

    @Value("${resume.import.strategy:direct}")
    private String importStrategy;

    public ResumeImportResponse importResume(MultipartFile file, String memberEmail) {
        MDC.put("spanId", "resume-import-service");

        validateFile(file);
        
        log.info("이력서 변환 시작 - 전략: {}, 파일명: {}, 크기: {}bytes",
                importStrategy, file.getOriginalFilename(), file.getSize());

        try {
            ResumeImportResponse response;
            if ("lambda".equals(importStrategy)) {
                response = importWithLambda(file);
            } else {
                response = importWithDirect(file);
            }
            
            // TODO: 일시적으로 저장 비활성화 - 변환 테스트만 진행
            log.info("이력서 변환 완료 (저장 비활성화) - 제목: {}", response.title());
            
            // 변환 성공 시에만 저장 시도 (나중에 활성화)
            /*
            try {
                saveConvertedResume(response, memberEmail);
                log.info("이력서 변환 및 저장 모두 완료 - 제목: {}", response.title());
            } catch (Exception saveException) {
                log.error("이력서 저장 실패, 하지만 변환 결과는 반환 - 오류: {}", saveException.getMessage());
                // 저장 실패해도 변환 결과는 반환 (선택사항)
            }
            */
            
            return response;
            
        } catch (Exception e) {
            log.error("이력서 변환 실패 - 파일: {}, 오류: {}", 
                    file.getOriginalFilename(), e.getMessage(), e);
            throw e;
        }
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("파일이 비어있습니다.");
        }

        String contentType = file.getContentType();
        if (contentType == null) {
            throw new IllegalArgumentException("파일 타입을 확인할 수 없습니다.");
        }

        // 지원하는 파일 타입 확인
        if (!contentType.contains("pdf") && !contentType.contains("image")) {
            throw new IllegalArgumentException("지원하지 않는 파일 형식입니다. PDF 또는 이미지 파일만 업로드 가능합니다.");
        }

        // 파일 크기 제한 (10MB)
        long maxSize = 10 * 1024 * 1024;
        if (file.getSize() > maxSize) {
            throw new IllegalArgumentException("파일 크기는 10MB를 초과할 수 없습니다.");
        }

        log.info("파일 검증 완료 - 타입: {}, 크기: {}bytes", contentType, file.getSize());
    }

    private ResumeImportResponse importWithDirect(MultipartFile file) {
        MDC.put("spanId", "resume-import-direct");
        
        log.info("Direct 전략으로 이력서 변환 시작");

        try {
            // 모든 파일 타입에 대해 동일한 처리 (단순화)
            return processFile(file);
            
        } catch (Exception e) {
            log.error("Direct 전략 변환 실패: {}", e.getMessage(), e);
            throw e;
        }
    }

    private ResumeImportResponse processFile(MultipartFile file) {
        // 1. Vision 프롬프트 생성 (Base64 이미지 포함)
        VisionPromptResult visionPrompt = resumeLlmPromptService.createVisionPrompt(file);

        // 2. Vision API 호출
        MDC.put("spanId", "resume-llm-vision-client");
        ResumeImportResponse response = resumeLlmClientService.convertResumeWithVision(
                visionPrompt.textPrompt(), 
                visionPrompt.base64Image()
        );

        MDC.put("spanId", "resume-import-direct");
        log.info("Vision API 파일 변환 완료 - 이름: {}, 제목: {}", 
                response.name(), response.title());

        return response;
    }

    private ResumeImportResponse importWithLambda(MultipartFile file) {
        MDC.put("spanId", "resume-import-lambda");
        
        log.info("Lambda 전략으로 이력서 변환 시작");

        try {
            // 1. Lambda OCR로 텍스트 추출
            MDC.put("spanId", "lambda-ocr-service");
            String extractedText = lambdaService.invokeLambdaOcr(file);

            MDC.put("spanId", "resume-import-lambda");
            log.info("Lambda OCR 완료 - 추출된 텍스트 길이: {}chars", extractedText.length());

            // 2. 추출된 텍스트로 프롬프트 생성
            String prompt = resumeLlmPromptService.createResumeConversionPrompt(extractedText);

            // 3. LLM 호출
            MDC.put("spanId", "resume-llm-client");
            ResumeImportResponse response = resumeLlmClientService.convertResume(prompt);

            MDC.put("spanId", "resume-import-lambda");
            log.info("Lambda 전략 변환 완료 - 이름: {}, 제목: {}", 
                    response.name(), response.title());

            return response;

        } catch (Exception e) {
            log.error("Lambda 전략 변환 실패, Direct 전략으로 대체: {}", e.getMessage());
            // Lambda 실패 시 Direct 전략으로 fallback
            return importWithDirect(file);
        }
    }

    @Transactional
    private void saveConvertedResume(ResumeImportResponse response, String memberEmail) {
        MDC.put("spanId", "resume-save-after-import");
        
        try {
            // ResumeImportResponse를 ResumeSaveRequest로 변환
            var saveRequest = response.toResumeSaveRequest();
            
            // 기존 ResumeService의 saveResume 메서드 사용
            resumeService.saveResume(saveRequest, memberEmail);
            
            log.info("변환된 이력서 저장 완료 - 제목: {}, 이름: {}", 
                    response.title(), response.name());
                    
        } catch (Exception e) {
            log.error("변환된 이력서 저장 실패: {}", e.getMessage(), e);
            // 저장 실패 시 예외를 다시 던짐
            throw new RuntimeException("이력서 저장 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }
}