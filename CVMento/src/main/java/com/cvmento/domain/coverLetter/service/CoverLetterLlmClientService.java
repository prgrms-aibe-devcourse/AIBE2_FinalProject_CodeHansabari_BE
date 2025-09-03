package com.cvmento.domain.coverLetter.service;

import com.cvmento.domain.coverLetter.dto.request.LlmRequest;
import com.cvmento.domain.coverLetter.dto.response.LlmAnalysisResponse;
import com.cvmento.domain.coverLetter.client.CoverLetterLlmFeignClient;
import com.cvmento.global.common.util.OpenAiResponseParser;
import com.cvmento.global.exception.customException.CoverLetterAiException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
            log.info("=== LLM API 요청 시작 ===");
            log.info("요청 모델: {}", request.model());
            log.info("요청 프롬프트 길이: {}", request.input().length());

            String rawResponse = getRawResponse(request);
            log.info("=== 원본 응답 받음 ===");

            LlmAnalysisResponse response = parseResponse(rawResponse);

            log.info("=== 변환된 응답 ===");
            log.info("피드백 길이: {}", response.feedback() != null ? response.feedback().length() : "null");
            log.info("개선된 내용 길이: {}", response.improvedContent() != null ? response.improvedContent().length() : "null");

            return response;

        } catch (Exception e) {
            log.error("LLM API 호출 실패", e);
            throw new CoverLetterAiException("LLM 서비스 호출에 실패했습니다.", e);
        }
    }

    private String getRawResponse(LlmRequest request) {
        try {
            return coverLetterLlmFeignClient.analyzeRaw(request);
        } catch (Exception e) {
            log.error("Raw 응답 받기 실패", e);
            throw new CoverLetterAiException("LLM API 호출 실패", e);
        }
    }

    private LlmAnalysisResponse parseResponse(String rawResponse) {
        try {
            String textContent = openAiResponseParser.extractTextContent(rawResponse);
            return parseActualContent(textContent);

        } catch (Exception e) {
            log.error("OpenAI 응답 파싱 실패: {}", e.getMessage());
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

                log.info("실제 content 파싱 성공 - feedback: {} chars, improved: {} chars",
                        feedback.length(), improvedContent.length());

                return new LlmAnalysisResponse(feedback, improvedContent);
            } else {
                // JSON이 아닌 경우 전체를 improvedContent로 사용
                log.info("JSON이 아닌 텍스트 - 전체를 improvedContent로 사용");
                return new LlmAnalysisResponse("", text);
            }
        } catch (Exception e) {
            log.error("실제 content 파싱 실패: {}", e.getMessage());
            return new LlmAnalysisResponse("", text);
        }
    }
}