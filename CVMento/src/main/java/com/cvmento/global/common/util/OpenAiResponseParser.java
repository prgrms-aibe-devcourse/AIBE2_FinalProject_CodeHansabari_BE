package com.cvmento.global.common.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OpenAiResponseParser {

    private final ObjectMapper objectMapper;

    /**
     * OpenAI /responses API 응답에서 실제 텍스트 컨텐츠 추출
     * 구조: output[].type="message".content[0].text
     */
    public String extractTextContent(String rawResponse) throws Exception {
        if (rawResponse == null || rawResponse.trim().isEmpty()) {
            throw new IllegalArgumentException("응답이 비어있습니다");
        }

        JsonNode jsonNode = objectMapper.readTree(rawResponse);

        if (!jsonNode.has("output")) {
            throw new IllegalArgumentException("output 필드를 찾을 수 없습니다");
        }

        JsonNode outputArray = jsonNode.get("output");
        if (!outputArray.isArray()) {
            throw new IllegalArgumentException("output이 배열이 아닙니다");
        }

        // output 배열에서 type이 "message"인 항목 찾기
        for (JsonNode outputItem : outputArray) {
            if (outputItem.has("type") && "message".equals(outputItem.get("type").asText())) {

                if (outputItem.has("content")) {
                    JsonNode contentArray = outputItem.get("content");
                    if (contentArray.isArray() && !contentArray.isEmpty()) {
                        JsonNode firstContent = contentArray.get(0);
                        if (firstContent.has("text")) {
                            String text = firstContent.get("text").asText();
                            log.info("OpenAI output에서 text 추출 성공: {} chars", text.length());
                            return text;
                        }
                    }
                }
            }
        }

        throw new IllegalArgumentException("message 타입의 텍스트 컨텐츠를 찾을 수 없습니다");
    }
}