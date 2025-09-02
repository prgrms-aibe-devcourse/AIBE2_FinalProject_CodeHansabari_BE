package com.cvmento.domain.interview.service;

import com.cvmento.domain.coverLetter.dto.request.LlmRequest;
import com.cvmento.domain.interview.client.InterviewLlmFeignClient;
import com.cvmento.global.exception.customException.InterviewException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class InterviewLlmClientService {

    private final InterviewLlmFeignClient interviewLlmFeignClient;
    private final ObjectMapper objectMapper;

    public String generateQnaList(String prompt) {
        try {
            log.info("=== 질문/답변 생성 API 요청 시작 ===");

            LlmRequest request = new LlmRequest("gpt-5-nano", prompt);
            String rawResponse = interviewLlmFeignClient.analyzeRaw(request);

            log.info("=== 원본 응답 받음 ===");

            String parsedResponse = parseOpenAiResponse(rawResponse);

            log.info("=== 질문/답변 생성 완료 ===");

            return parsedResponse;

        } catch (Exception e) {
            log.error("질문/답변 생성 API 호출 실패", e);
            throw new InterviewException("면접 질문/답변 생성에 실패했습니다.", e);
        }
    }

    private String parseOpenAiResponse(String rawResponse) {
        if (rawResponse == null || rawResponse.trim().isEmpty()) {
            log.warn("응답이 비어있습니다");
            return "";
        }

        try {
            var jsonNode = objectMapper.readTree(rawResponse);

            if (jsonNode.has("output")) {
                var outputArray = jsonNode.get("output");
                if (outputArray.isArray()) {
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
                                        return text;
                                    }
                                }
                            }
                        }
                    }
                }
            }

            log.warn("예상된 구조를 찾지 못함 - 전체 응답을 사용");
            return rawResponse;

        } catch (Exception e) {
            log.error("JSON 파싱 실패: {}", e.getMessage());
            return rawResponse;
        }
    }
}