package com.cvmento.domain.resume.service;

import com.cvmento.domain.resume.dto.response.ResumeLlmResponse;
import com.cvmento.domain.member.entity.Member;
import com.cvmento.domain.member.repository.MemberRepository;
import com.cvmento.domain.resume.dto.request.ResumeCreateRequest;
import com.cvmento.domain.resume.dto.response.ResumeResponse;
import com.cvmento.domain.resume.exception.ResumeFileException;
import com.cvmento.global.exception.customException.ResumeAiException;
import com.cvmento.global.aws.LambdaService;
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

/**
 * 이력서 파일 가져오기 서비스
 * 
 * 기능: 사용자가 업로드한 이력서 파일(PDF/이미지)을 분석하여 
 *      사이트의 이력서 형식으로 자동 변환하는 서비스
 * 
 * 처리 방식:
 * 1. Lambda 방식: AWS Lambda로 OCR 처리 → 텍스트 추출 → LLM으로 구조화
 * 2. Direct 방식: Vision LLM에 파일을 직접 전송하여 한 번에 처리
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ResumeImportService {

    // === 의존성 주입 ===
    private final MemberRepository memberRepository;           // 사용자 정보 조회
    private final ResumeService resumeService;                // 이력서 생성 로직
    private final ResumeLlmClientService llmClientService;    // LLM API 통신
    private final ResumeLlmPromptService llmPromptService;    // LLM 프롬프트 생성

    private final LambdaService lambdaService;                // AWS Lambda OCR 서비스
    private final ObjectMapper objectMapper;                 // JSON 파싱

    /**
     * 처리 전략 설정 (application.yml에서 설정)
     * - "lambda": AWS Lambda OCR → LLM 텍스트 처리
     * - "direct": Vision LLM 직접 처리 (기본값)
     */
    @Value("${resume.import.strategy:direct}") 
    private String importStrategy;

    /**
     * 메인 진입점: 업로드된 이력서 파일을 분석하여 새로운 이력서 생성
     * 
     * @param file 업로드된 이력서 파일 (PDF 또는 이미지)
     * @param userEmail 사용자 이메일
     * @return 생성된 이력서 응답 데이터
     */
    @Transactional
    public ResumeResponse createResumeFromFile(MultipartFile file, String userEmail) {
        // === 1단계: 공통 전처리 작업 ===
        validateFile(file);  // 파일 유효성 검사 (크기, 형식 등)
        log.info("File validation successful for user: {}. Strategy: {}", userEmail, importStrategy);

        // 사용자 정보 조회 및 검증
        Member member = memberRepository.findByEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("Member not found with email: " + userEmail));

        // === 2단계: 설정에 따른 처리 방식 선택 ===
        if ("lambda".equalsIgnoreCase(importStrategy)) {
            // AWS Lambda OCR 방식으로 처리
            return createResumeViaLambda(file, userEmail);
        } else {
            // Vision LLM 직접 처리 방식 (기본값)
            return createResumeDirectly(file, userEmail);
        }
    }

    /**
     * Lambda 방식: AWS Lambda OCR → LLM 텍스트 처리
     * 
     * 처리 흐름:
     * 1. AWS Lambda로 OCR 수행 (이미지/PDF → 텍스트)
     * 2. 추출된 텍스트를 LLM에 전달
     * 3. LLM이 텍스트를 이력서 구조로 변환
     * 4. 변환된 데이터로 이력서 생성
     */
    private ResumeResponse createResumeViaLambda(MultipartFile file, String userEmail) {
        log.info("Lambda OCR 방식으로 이력서 생성 시작. 사용자: {}", userEmail);

        try {
            // === 1단계: AWS Lambda OCR로 텍스트 추출 ===
            String ocrText = lambdaService.invokeLambdaOcr(file);

            // OCR 결과 검증
            if (ocrText == null || ocrText.trim().isEmpty()) {
                log.error("Lambda OCR 결과가 비어있음. 사용자: {}", userEmail);
                throw new ResumeAiException("파일에서 텍스트를 추출하지 못했습니다.");
            }

            log.info("OCR 완료. 추출된 텍스트 길이: {} chars", ocrText.length());

            // === 2단계: LLM으로 텍스트 구조화 ===
            // 추출된 텍스트를 이력서 형식으로 변환하는 프롬프트 생성
            String prompt = llmPromptService.buildResumeTextImportPrompt(ocrText);
            
            // LLM API 호출 (텍스트만 처리, 이미지 없음)
            ResumeLlmResponse llmResponse = llmClientService.analyzeUniversal(prompt, null, null);

            // LLM 응답 검증
            String extractedJson = llmResponse.response();
            if (extractedJson == null || extractedJson.trim().isEmpty()) {
                log.error("LLM 응답이 비어있음. 사용자: {}", userEmail);
                throw new ResumeAiException("AI가 이력서 내용을 분석하지 못했습니다.");
            }

            // === 3단계: JSON을 이력서 생성 요청 객체로 변환 ===
            ResumeCreateRequest createRequest = objectMapper.readValue(extractedJson, ResumeCreateRequest.class);

            // === 4단계: 실제 이력서 생성 ===
            return resumeService.createResume(createRequest, userEmail);

        } catch (JsonProcessingException e) {
            log.error("JSON 파싱 오류. 사용자: {}, 오류: {}", userEmail, e.getMessage(), e);
            throw new ResumeAiException("AI 응답을 처리하는 중 오류가 발생했습니다.", e);
        } catch (Exception e) {
            log.error("Lambda 이력서 생성 오류. 사용자: {}, 오류: {}", userEmail, e.getMessage(), e);
            throw new ResumeAiException("이력서 생성 중 오류가 발생했습니다.", e);
        }
    }

    /**
     * Direct 방식: Vision LLM 직접 처리 (기본 방식)
     * 
     * 처리 흐름:
     * 1. 업로드된 파일을 Base64로 인코딩
     * 2. Vision LLM에 이미지와 프롬프트를 함께 전송
     * 3. LLM이 이미지를 직접 분석하여 이력서 구조로 변환
     * 4. 변환된 데이터로 이력서 생성
     */
    private ResumeResponse createResumeDirectly(MultipartFile file, String userEmail) {
        log.info("Direct Vision LLM 방식으로 이력서 생성 시작. 사용자: {}", userEmail);
        
        // PDF와 이미지 모두 처리
        log.info("Direct Vision LLM 방식으로 {} 파일 처리 중", file.getContentType());
        
        String extractedJson = null; // 변수를 try 블록 바깥에 선언
        
        try {
            // === 1단계: 파일을 Base64로 변환 ===
            // Vision LLM이 이미지를 처리할 수 있도록 Base64 인코딩
            String base64Image = Base64.getEncoder().encodeToString(file.getBytes());
            log.info("File converted to Base64. Size: {} bytes", base64Image.length());

            // === 2단계: Vision LLM용 프롬프트 생성 ===
            // 이미지에서 이력서 내용을 추출하라는 프롬프트
            String prompt = llmPromptService.buildResumeImportPrompt();

            // === 3단계: Vision LLM API 호출 ===
            // 프롬프트 + Base64 이미지 + 파일 타입을 함께 전송
            ResumeLlmResponse llmResponse = llmClientService.analyzeUniversal(
                prompt,                    // 프롬프트
                base64Image,              // Base64 인코딩된 이미지
                file.getContentType()     // 파일 MIME 타입 (image/png, application/pdf 등)
            );
            
            extractedJson = llmResponse.response();

            // LLM 응답 검증
            if (extractedJson == null || extractedJson.trim().isEmpty()) {
                log.error("LLM returned empty content for user: {}", userEmail);
                throw new ResumeAiException("AI가 이력서 내용을 추출하지 못했습니다. 다른 파일로 시도해주세요.");
            }
            log.info("LLM analysis complete. Received JSON content.");
            log.debug("LLM 응답 JSON: {}", extractedJson); // JSON 내용 로깅

            // === 4단계: JSON을 이력서 생성 요청 객체로 변환 및 생성 ===
            ResumeCreateRequest createRequest = objectMapper.readValue(extractedJson, ResumeCreateRequest.class);
            
            // 변환된 객체 검증 로깅
            log.debug("Parsed ResumeCreateRequest - title: {}, memberInfo: {}, sections count: {}", 
                createRequest.title(), 
                createRequest.memberInfo() != null ? "present" : "null", 
                createRequest.sections() != null ? createRequest.sections().size() : "null");
            return resumeService.createResume(createRequest, userEmail);

        } catch (JsonProcessingException e) {
            log.error("Failed to parse JSON from LLM response for user: {}. Error: {}", userEmail, e.getMessage(), e);
            log.error("Problematic JSON content: {}", extractedJson); // 문제가 된 JSON 로깅
            throw new ResumeAiException("AI 응답을 처리하는 중 오류가 발생했습니다. 응답 형식이 올바르지 않을 수 있습니다.", e);
        } catch (IOException e) {
            log.error("Failed to read file for user: {}", userEmail, e);
            throw new RuntimeException("파일을 읽는 중 오류가 발생했습니다.", e);
        }
    }

    /**
     * 업로드된 파일의 유효성을 검증
     * 
     * 검증 항목:
     * 1. 파일이 비어있지 않은지 확인
     * 2. 지원되는 파일 형식인지 확인 (PDF, 이미지)
     * 3. 파일 크기가 제한 내인지 확인 (5MB 이하)
     */
    private void validateFile(MultipartFile file) {
        // === 1. 파일 존재 여부 검증 ===
        if (file.isEmpty()) {
            throw new ResumeFileException("업로드된 파일이 비어있습니다.");
        }
        
        // === 2. 파일 형식 검증 ===
        String contentType = file.getContentType();
        if (contentType == null || 
            (!contentType.equals("application/pdf") && !contentType.startsWith("image/"))) {
            throw new ResumeFileException("지원하지 않는 파일 형식입니다. PDF, PNG, JPG 파일만 업로드 가능합니다.");
        }
        
        // === 3. 파일 크기 검증 ===
        long maxSize = 5 * 1024 * 1024; // 5MB 제한
        if (file.getSize() > maxSize) {
            throw new ResumeFileException("파일 크기가 5MB를 초과합니다.");
        }
    }
}
