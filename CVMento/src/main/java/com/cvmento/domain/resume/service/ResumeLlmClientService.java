package com.cvmento.domain.resume.service;

import com.cvmento.domain.resume.client.ResumeLlmFeignClient;
import com.cvmento.domain.resume.dto.request.ResumeLlmRequest;
import com.cvmento.domain.resume.dto.response.ResumeImportResponse;
import com.cvmento.global.common.util.OpenAiResponseParser;
import com.cvmento.global.exception.customException.ResumeException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ResumeLlmClientService {

    private final ResumeLlmFeignClient resumeLlmFeignClient;
    private final ObjectMapper objectMapper;
    private final OpenAiResponseParser openAiResponseParser;

    public ResumeImportResponse convertResume(String prompt) {
        MDC.put("spanId", "resume-llm-client");

        validatePrompt(prompt);
        ResumeLlmRequest request = createLlmRequest(prompt);
        return callLlmApi(request);
    }


    private void validatePrompt(String prompt) {
        if (prompt == null || prompt.trim().isEmpty()) {
            throw new IllegalArgumentException("프롬프트가 비어있습니다.");
        }
    }

    private ResumeLlmRequest createLlmRequest(String prompt) {
        return ResumeLlmRequest.create("gpt-5-nano", prompt);
    }

    private ResumeImportResponse callLlmApi(ResumeLlmRequest request) {
        try {
            log.info("Resume LLM API 요청 시작 - 모델: {}", request.model());

            MDC.put("spanId", "openai-resume-api");
            String rawResponse = getRawResponse(request);

            MDC.put("spanId", "resume-response-parsing");
            log.info("Resume LLM 원본 응답 수신 완료 - 응답길이: {}", rawResponse.length());

            ResumeImportResponse response = parseOpenAiResponse(rawResponse);

            MDC.put("spanId", "resume-llm-client");
            log.info("Resume 응답 파싱 완료 - 이름: {}, 제목: {}",
                    response.name(), response.title());

            return response;

        } catch (Exception e) {
            log.error("Resume LLM API 호출 실패: {}", e.getMessage(), e);
            throw new ResumeException("이력서 변환에 실패했습니다.", e);
        }
    }

    private String getRawResponse(ResumeLlmRequest request) {
        try {
            return resumeLlmFeignClient.analyzeRaw(request);
        } catch (Exception e) {
            log.error("LLM API 원본 응답 받기 실패: {}", e.getMessage());
            throw new ResumeException("Resume LLM API 호출 실패", e);
        }
    }

    private ResumeImportResponse parseOpenAiResponse(String rawResponse) {
        try {
            MDC.put("spanId", "openai-response-parser");
            String textContent = openAiResponseParser.extractTextContent(rawResponse);

            MDC.put("spanId", "resume-response-parsing");
            return parseActualContent(textContent);

        } catch (Exception e) {
            log.error("Resume OpenAI 응답 파싱 실패: {}", e.getMessage());
            throw new ResumeException("LLM 응답 파싱에 실패했습니다.", e);
        }
    }

    private ResumeImportResponse parseActualContent(String text) {
        try {
            log.debug("Resume 응답 파싱 시작 - 텍스트길이: {}", text.length());
            log.debug("원본 응답 텍스트: {}", text);

            // 마크다운 코드 블록 제거
            String cleanText = text.trim()
                    .replaceAll("```json\\s*", "")
                    .replaceAll("```\\s*json\\s*", "")
                    .replaceAll("\\s*```", "")
                    .trim();

            log.debug("정제된 텍스트: {}", cleanText);

            // JSON 찾기 시도 - { 로 시작하는 부분 찾기
            int jsonStart = cleanText.indexOf("{");
            if (jsonStart != -1) {
                // 마지막 } 찾기
                int jsonEnd = cleanText.lastIndexOf("}");
                if (jsonEnd != -1 && jsonEnd > jsonStart) {
                    String jsonText = cleanText.substring(jsonStart, jsonEnd + 1);
                    log.debug("추출된 JSON: {}", jsonText);
                    
                    try {
                        ResumeImportResponse response = objectMapper.readValue(jsonText, ResumeImportResponse.class);
                        log.info("Resume JSON 파싱 성공 - 이름: {}, 경력타입: {}", 
                                response.name(), response.careerType());
                        return response;
                    } catch (Exception jsonEx) {
                        log.error("JSON 파싱 오류: {}", jsonEx.getMessage());
                        log.error("파싱 시도한 JSON: {}", jsonText);
                    }
                }
            }

            // JSON을 찾을 수 없는 경우 기본값 반환
            log.warn("JSON을 찾을 수 없어 기본 응답 생성 - 원본 텍스트: {}", text);
            return createDefaultResponse(text);

        } catch (Exception e) {
            log.error("Resume content 파싱 실패: {}", e.getMessage());
            return createDefaultResponse(text);
        }
    }

    private ResumeImportResponse createDefaultResponse(String originalText) {
        log.info("기본 이력서 응답 생성");
        return new ResumeImportResponse(
                "변환된 이력서",
                com.cvmento.domain.resume.enums.ResumeType.DEFAULT,
                "이름 미상",
                "email@example.com",
                1990,
                "010-0000-0000",
                com.cvmento.domain.resume.enums.CareerType.FRESHMAN,
                "개발자",
                originalText.length() > 500 ? originalText.substring(0, 500) + "..." : originalText,
                null, null, null,
                java.util.List.of(), java.util.List.of(), java.util.List.of(),
                java.util.List.of(), java.util.List.of(), java.util.List.of(),
                java.util.List.of()
        );
    }
}