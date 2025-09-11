package com.cvmento.domain.coverLetter.service;

import com.cvmento.domain.coverLetter.dto.request.LlmRequest;
import com.cvmento.domain.coverLetter.dto.response.LlmAnalysisResponse;
import com.cvmento.domain.coverLetter.client.CoverLetterLlmFeignClient;
import com.cvmento.global.common.util.OpenAiResponseParser;
import com.cvmento.global.exception.customException.CoverLetterAiException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;

/**
 * LLM 클라이언트 서비스 - 올바른 파싱
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CoverLetterLlmClientService {

    private final CoverLetterLlmFeignClient coverLetterLlmFeignClient;
    private final ObjectMapper objectMapper;
    private final OpenAiResponseParser openAiResponseParser;

    public LlmAnalysisResponse analyze(String prompt) {
        MDC.put("spanId", "llm-client-service");

        validatePrompt(prompt);
        LlmRequest request = createLlmRequest(prompt);
        return callLlmApi(request);
    }

    private void validatePrompt(String prompt) {
        if (prompt == null || prompt.trim().isEmpty()) {
            throw new IllegalArgumentException("프롬프트가 비어있습니다.");
        }
    }

    private LlmRequest createLlmRequest(String prompt) {
        return new LlmRequest(
                "gpt-5-nano",
                prompt
        );
    }

    private LlmAnalysisResponse callLlmApi(LlmRequest request) {
        try {
            log.info("LLM API 요청 시작 - 모델: {}, 프롬프트길이: {}",
                    request.model(), request.input().length());

            MDC.put("spanId", "openai-llm-api");
            String rawResponse = getRawResponse(request);

            MDC.put("spanId", "llm-response-parsing");
            log.info("LLM 원본 응답 수신 완료 - 응답길이: {}", rawResponse.length());

            LlmAnalysisResponse response = parseResponse(rawResponse);

            MDC.put("spanId", "llm-client-service");
            log.info("LLM 응답 파싱 완료 - 피드백길이: {}, 개선내용길이: {}",
                    response.feedback() != null ? response.feedback().length() : 0,
                    response.improvedContent() != null ? response.improvedContent().length() : 0);

            return response;

        } catch (Exception e) {
            log.error("LLM API 호출 실패 - 모델: {}, 오류: {}", request.model(), e.getMessage(), e);
            throw new CoverLetterAiException("LLM 서비스 호출에 실패했습니다.", e);
        }
    }

    private String getRawResponse(LlmRequest request) {
        try {
            return coverLetterLlmFeignClient.analyzeRaw(request);
        } catch (Exception e) {
            log.error("LLM API 원본 응답 받기 실패: {}", e.getMessage());
            throw new CoverLetterAiException("LLM API 호출 실패", e);
        }
    }

    private LlmAnalysisResponse parseResponse(String rawResponse) {
        try {
            MDC.put("spanId", "openai-response-parser");
            String textContent = openAiResponseParser.extractTextContent(rawResponse);

            MDC.put("spanId", "llm-response-parsing");
            return parseActualContent(textContent);

        } catch (Exception e) {
            log.error("OpenAI 응답 파싱 실패 - 응답길이: {}, 오류: {}",
                    rawResponse.length(), e.getMessage());
            throw new CoverLetterAiException("LLM 응답 파싱에 실패했습니다.", e);
        }
    }

    private LlmAnalysisResponse parseActualContent(String text) {
        try {
            // text가 JSON 형식인지 확인하고 파싱
            if (text.trim().startsWith("{")) {
                var contentJson = objectMapper.readTree(text);

                String feedback = "";
                String improvedContent = "";

                // feedback 객체를 문자열로 변환
                if (contentJson.has("feedback")) {
                    feedback = contentJson.get("feedback").toString();
                }

                // improvedContent를 문자열로 추출
                if (contentJson.has("improvedContent")) {
                    improvedContent = contentJson.get("improvedContent").asText();
                }

                log.info("JSON 파싱 성공 - feedback: {}chars, improved: {}chars",
                        feedback.length(), improvedContent.length());

                return new LlmAnalysisResponse(feedback, improvedContent);
            } else {
                // JSON이 아닌 경우 전체를 improvedContent로 사용
                log.warn("JSON 형식이 아닌 응답 - 전체를 improvedContent로 처리");
                return new LlmAnalysisResponse("", text);
            }
        } catch (Exception e) {
            log.error("실제 content 파싱 실패 - 텍스트길이: {}, 오류: {}",
                    text.length(), e.getMessage());
            return new LlmAnalysisResponse("", text);
        }
    }
}