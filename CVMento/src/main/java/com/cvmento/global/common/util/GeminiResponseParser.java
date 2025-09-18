package com.cvmento.global.common.util;

import com.cvmento.domain.coverLetter.dto.response.GeminiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class GeminiResponseParser {

    private final ObjectMapper objectMapper;

    /**
     * Gemini API 응답에서 텍스트 컨텐츠를 추출합니다.
     * @param response Gemini API 응답 JSON 문자열
     * @return 추출된 텍스트 컨텐츠
     */
    public String extractTextContent(String response) {
        try {
            log.debug("Gemini 응답 파싱 시작: {}자", response.length());
            log.debug("Gemini 원본 응답: {}", response);
            
            GeminiResponse geminiResponse = objectMapper.readValue(response, GeminiResponse.class);
            
            if (geminiResponse.candidates() == null || geminiResponse.candidates().isEmpty()) {
                log.warn("Gemini 응답에 candidates가 없습니다.");
                return "";
            }
            
            GeminiResponse.Candidate candidate = geminiResponse.candidates().get(0);
            
            // finishReason 확인
            if (candidate.finishReason() != null) {
                log.warn("Gemini 응답 finishReason: {}", candidate.finishReason());
                if ("MAX_TOKENS".equals(candidate.finishReason())) {
                    log.warn("Gemini 응답이 토큰 한계로 잘렸습니다. maxOutputTokens를 늘려주세요.");
                }
            }
            
            if (candidate.content() == null || candidate.content().parts() == null || candidate.content().parts().isEmpty()) {
                log.warn("Gemini 응답에 content 또는 parts가 없습니다.");
                return "";
            }
            
            List<GeminiResponse.Part> parts = candidate.content().parts();
            StringBuilder textContent = new StringBuilder();
            
            for (GeminiResponse.Part part : parts) {
                if (part.text() != null) {
                    textContent.append(part.text());
                }
            }
            
            String result = textContent.toString();
            log.debug("Gemini 텍스트 컨텐츠 추출 완료: {}자", result.length());
            
            return result;
            
        } catch (Exception e) {
            log.error("Gemini 응답 파싱 실패", e);
            return "";
        }
    }
}
