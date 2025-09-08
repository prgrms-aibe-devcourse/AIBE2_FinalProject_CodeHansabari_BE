package com.cvmento.domain.resume.service;

import com.cvmento.domain.coverLetter.dto.response.LlmAnalysisResponse;
import com.cvmento.domain.member.entity.Member;
import com.cvmento.domain.member.repository.MemberRepository;
import com.cvmento.domain.resume.dto.request.ResumeCreateRequest;
import com.cvmento.domain.resume.dto.response.ResumeResponse;
import com.cvmento.global.exception.customException.CoverLetterAiException;
import com.cvmento.global.aws.LambdaService;
import com.cvmento.global.aws.S3Service;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ResumeImportService {

    private final MemberRepository memberRepository;
    private final ResumeService resumeService;
    private final ResumeLlmClientService llmClientService;
    private final ResumeLlmPromptService llmPromptService;
    private final S3Service s3Service;
    private final LambdaService lambdaService;
    private final ObjectMapper objectMapper;

    @Value("${cloud.aws.s3.bucket}")
    private String s3BucketName;

    @Value("${resume.import.strategy:direct}") // Default to 'direct'
    private String importStrategy;

    @Transactional
    public ResumeResponse createResumeFromFile(MultipartFile file, String userEmail) {
        // Common steps for both strategies
        validateFile(file);
        log.info("File validation successful for user: {}. Strategy: {}", userEmail, importStrategy);

        Member member = memberRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("Member not found with email: " + userEmail));

        if ("lambda".equalsIgnoreCase(importStrategy)) {
            return createResumeViaLambda(file, userEmail);
        } else {
            return createResumeDirectly(file, userEmail);
        }
    }

    private ResumeResponse createResumeViaLambda(MultipartFile file, String userEmail) {
        log.info("Lambda OCR 방식으로 이력서 생성 시작. 사용자: {}", userEmail);

        try {
            // Lambda OCR 호출 (S3 없이 직접 호출)
            String ocrText = lambdaService.invokeLambdaOcr(file);

            if (ocrText == null || ocrText.trim().isEmpty()) {
                log.error("Lambda OCR 결과가 비어있음. 사용자: {}", userEmail);
                throw new CoverLetterAiException("파일에서 텍스트를 추출하지 못했습니다.");
            }

            log.info("OCR 완료. 추출된 텍스트 길이: {} chars", ocrText.length());

            // LLM으로 구조화
            String prompt = llmPromptService.buildResumeTextImportPrompt(ocrText);
            LlmAnalysisResponse llmResponse = llmClientService.analyzeUniversal(prompt, null, null);

            String extractedJson = llmResponse.improvedContent();
            if (extractedJson == null || extractedJson.trim().isEmpty()) {
                log.error("LLM 응답이 비어있음. 사용자: {}", userEmail);
                throw new CoverLetterAiException("AI가 이력서 내용을 분석하지 못했습니다.");
            }

            // JSON을 객체로 변환
            ResumeCreateRequest createRequest = objectMapper.readValue(extractedJson, ResumeCreateRequest.class);

            // 이력서 생성
            return resumeService.createResume(createRequest, userEmail);

        } catch (JsonProcessingException e) {
            log.error("JSON 파싱 오류. 사용자: {}, 오류: {}", userEmail, e.getMessage(), e);
            throw new CoverLetterAiException("AI 응답을 처리하는 중 오류가 발생했습니다.", e);
        } catch (Exception e) {
            log.error("Lambda 이력서 생성 오류. 사용자: {}, 오류: {}", userEmail, e.getMessage(), e);
            throw new CoverLetterAiException("이력서 생성 중 오류가 발생했습니다.", e);
        }
    }

    private ResumeResponse createResumeDirectly(MultipartFile file, String userEmail) {
        log.info("Starting resume creation via direct LLM for user: {}", userEmail);
        try {
            // Step 1: Convert file to Base64
            String base64Image = Base64.getEncoder().encodeToString(file.getBytes());
            log.info("File converted to Base64. Size: {} bytes", base64Image.length());

            // Step 2: Build prompt for Vision LLM
            String prompt = llmPromptService.buildResumeImportPrompt();

            // Step 3: Call Vision LLM service
            LlmAnalysisResponse llmResponse = llmClientService.analyzeUniversal(prompt, base64Image, file.getContentType());
            String extractedJson = llmResponse.improvedContent();

            if (extractedJson == null || extractedJson.trim().isEmpty()) {
                log.error("LLM returned empty content for user: {}", userEmail);
                throw new CoverLetterAiException("AI가 이력서 내용을 추출하지 못했습니다. 다른 파일로 시도해주세요.");
            }
            log.info("LLM analysis complete. Received JSON content.");

            // Step 4: Parse and create resume
            ResumeCreateRequest createRequest = objectMapper.readValue(extractedJson, ResumeCreateRequest.class);
            return resumeService.createResume(createRequest, userEmail);

        } catch (JsonProcessingException e) {
            log.error("Failed to parse JSON from LLM response for user: {}. Error: {}", userEmail, e.getMessage(), e);
            throw new CoverLetterAiException("AI 응답을 처리하는 중 오류가 발생했습니다. 응답 형식이 올바르지 않을 수 있습니다.", e);
        } catch (IOException e) {
            log.error("Failed to read file for user: {}", userEmail, e);
            throw new RuntimeException("파일을 읽는 중 오류가 발생했습니다.", e);
        }
    }

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Uploaded file is empty.");
        }
        String contentType = file.getContentType();
        if (contentType == null || (!contentType.equals("application/pdf") && !contentType.startsWith("image/"))) {
            throw new IllegalArgumentException("Invalid file type. Only PDF, PNG, JPG files are allowed.");
        }
        long maxSize = 5 * 1024 * 1024; // 5MB
        if (file.getSize() > maxSize) {
            throw new IllegalArgumentException("File size exceeds the limit of 5MB.");
        }
    }
}
