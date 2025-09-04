package com.cvmento.domain.resume.service;

import com.cvmento.domain.coverLetter.dto.response.LlmAnalysisResponse;
import com.cvmento.domain.member.entity.Member;
import com.cvmento.domain.member.repository.MemberRepository;
import com.cvmento.domain.resume.dto.request.ResumeCreateRequest;
import com.cvmento.domain.resume.dto.response.ResumeResponse;
import com.cvmento.global.exception.customException.CoverLetterAiException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Base64;

@Service
@RequiredArgsConstructor
@Slf4j
public class ResumeImportService {

    private final MemberRepository memberRepository;
    private final ResumeService resumeService;
    private final ResumeLlmClientService llmClientService;
    private final ResumeLlmPromptService llmPromptService;
    private final ObjectMapper objectMapper;

    @Transactional
    public ResumeResponse createResumeFromFile(MultipartFile file, String userEmail) {
        // Step 1: Validate file (size, type, etc.)
        validateFile(file);
        log.info("File validation successful for user: {}", userEmail);

        // Step 2: Find member
        Member member = memberRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("Member not found with email: " + userEmail));

        try {
            // Step 3: Convert file to Base64
            String base64Image = Base64.getEncoder().encodeToString(file.getBytes());
            log.info("File converted to Base64 successfully. Size: {} bytes", base64Image.length());

            // Step 4: Build prompt for Vision LLM
            String prompt = llmPromptService.buildResumeImportPrompt();

            // Step 5: Call LLM service
            log.info("Sending resume image to Vision LLM for analysis.");
            LlmAnalysisResponse llmResponse = llmClientService.analyzeUniversal(prompt, base64Image, file.getContentType());
            String extractedJson = llmResponse.improvedContent();

            if (extractedJson == null || extractedJson.trim().isEmpty()) {
                log.error("LLM returned empty content for user: {}", userEmail);
                throw new CoverLetterAiException("AI가 이력서 내용을 추출하지 못했습니다. 다른 파일로 시도해주세요.");
            }
            log.info("LLM analysis complete. Received JSON content.");

            // Step 6: Parse LLM response into a ResumeCreateRequest DTO
            ResumeCreateRequest createRequest = objectMapper.readValue(extractedJson, ResumeCreateRequest.class);
            log.info("Successfully parsed LLM response into ResumeCreateRequest DTO.");

            // Step 7: Create resume using the existing ResumeService logic
            return resumeService.createResume(createRequest, userEmail);

        } catch (JsonProcessingException e) {
            log.error("Failed to parse JSON from LLM response for user: {}. JSON: {}", userEmail, e.getOriginalMessage(), e);
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
        // Optional: Add file size validation
        long maxSize = 5 * 1024 * 1024; // 5MB
        if (file.getSize() > maxSize) {
            throw new IllegalArgumentException("File size exceeds the limit of 5MB.");
        }
    }
}