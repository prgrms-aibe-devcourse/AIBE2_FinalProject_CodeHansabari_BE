package com.cvmento.domain.resume.service;

import com.cvmento.domain.coverLetter.dto.request.LlmRequest; // Reusing LlmRequest from coverLetter
import com.cvmento.domain.coverLetter.dto.response.LlmAnalysisResponse; // Reusing LlmAnalysisResponse from coverLetter
import com.cvmento.domain.resume.client.ResumeLlmFeignClient;
import com.cvmento.global.exception.customException.CoverLetterAiException; // Reusing exception
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * LLM 클라이언트 서비스 - 이력서 AI용
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ResumeLlmClientService {

    private final ResumeLlmFeignClient resumeLlmFeignClient;
    private final ObjectMapper objectMapper;

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
        // Model name should be configurable, but for now, use the same as cover letter
        return new LlmRequest(
                "gpt-5-nano",
                prompt
        );
    }

    private LlmAnalysisResponse callLlmApi(LlmRequest request) {
        try {
            log.info("=== Resume LLM API 요청 시작 ===");
            log.info("요청 모델: {}", request.model());
            log.info("요청 프롬프트 길이: {}", request.input().length());

            String rawResponse = resumeLlmFeignClient.analyzeRaw(request);
            log.info("=== 원본 응답 받음 ===");

            LlmAnalysisResponse response = parseOpenAiResponse(rawResponse);

            log.info("=== 변환된 응답 ====");
            log.info("개선된 내용 길이: {}", response.improvedContent() != null ? response.improvedContent().length() : "null");

            return response;

        } catch (FeignException e) {
            // FeignException은 LLM API 서버의 응답 오류(4xx, 5xx) 또는 네트워크 문제를 포함합니다.
            log.error("Resume LLM API 호출 중 Feign 오류 발생. Status: {}, Body: {}", e.status(), e.contentUTF8(), e);

            // 네트워크 연결 자체를 실패했을 경우를 확인 (e.g., ConnectException)
            if (e.getCause() instanceof java.net.ConnectException) {
                throw new CoverLetterAiException("AI 서비스에 연결할 수 없습니다. 네트워크 상태를 확인하거나 잠시 후 다시 시도해주세요.", e);
            }

            // 그 외 4xx, 5xx 등 API 서버에서 받은 오류
            throw new CoverLetterAiException("AI 서비스로부터 응답을 받는데 실패했습니다. (HTTP Status: " + e.status() + ")", e);

        } catch (Exception e) {
            // 그 외 예측하지 못한 예외
            log.error("Resume LLM API 호출 중 알 수 없는 오류 발생", e);
            throw new CoverLetterAiException("AI 서비스 요청 중 알 수 없는 오류가 발생했습니다.", e);
        }
    }

    // This parsing logic is directly copied from CoverLetterLlmClientService
    private LlmAnalysisResponse parseOpenAiResponse(String rawResponse) {
        if (rawResponse == null || rawResponse.trim().isEmpty()) {
            log.warn("응답이 비어있습니다");
            return new LlmAnalysisResponse("", "");
        }

        try {
            var jsonNode = objectMapper.readTree(rawResponse);

            // OpenAI /responses API의 실제 구조: output[1].content[0].text
            if (jsonNode.has("output")) {
                var outputArray = jsonNode.get("output");
                if (outputArray.isArray()) {

                    // output 배열에서 type이 "message"인 항목 찾기
                    for (var outputItem : outputArray) {
                        if (outputItem.has("type") &&
                                "message".equals(outputItem.get("type").asText())) {

                            if (outputItem.has("content")) {
                                var contentArray = outputItem.get("content");
                                if (contentArray.isArray() && contentArray.size() > 0) {
                                    var firstContent = contentArray.get(0);
                                    if (firstContent.has("text")) {
                                        String text = firstContent.get("text").asText();
                                        log.info("OpenAI output에서 text 추출 성공: {} chars", text.length());
                                        return parseActualContent(text);
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 구조를 찾지 못한 경우
            log.warn("예상된 구조를 찾지 못함 - 전체 응답을 사용");
            return new LlmAnalysisResponse("", rawResponse);

        } catch (Exception e) {
            log.error("JSON 파싱 실패: {}", e.getMessage());
            return new LlmAnalysisResponse("", rawResponse);
        }
    }

    // This parsing logic is directly copied from CoverLetterLlmClientService
    private LlmAnalysisResponse parseActualContent(String text) {
        String cleanedText = text.trim();

        // LLM이 응답을 마크다운 코드 블록으로 감싸는 경우가 많으므로, 이를 제거합니다.
        if (cleanedText.startsWith("```json")) {
            cleanedText = cleanedText.substring(7);
            if (cleanedText.endsWith("```")) {
                cleanedText = cleanedText.substring(0, cleanedText.length() - 3);
            }
            cleanedText = cleanedText.trim();
        } else if (!cleanedText.startsWith("{")) {
            // 마크다운 블록이 없지만, JSON 앞에 다른 텍스트가 있는 경우 첫 '{' 부터 사용합니다.
            int jsonStart = cleanedText.indexOf('{');
            if (jsonStart != -1) {
                cleanedText = cleanedText.substring(jsonStart);
            }
        }
        
        // 이력서 제안 기능의 프롬프트는 특정 JSON 구조를 요청합니다.
        // 이 메서드에서 'feedback'이나 'improvedContent'를 파싱하려고 시도하는 것은 올바르지 않습니다.
        // 정리된 JSON 문자열을 그대로 호출자(ResumeAiService)에게 전달하여 파싱하도록 합니다.
        log.info("LLM 응답에서 추출 및 정리된 내용 길이: {} chars", cleanedText.length());
        return new LlmAnalysisResponse("", cleanedText);
    }
}
