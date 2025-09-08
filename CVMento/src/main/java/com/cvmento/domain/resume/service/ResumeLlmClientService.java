package com.cvmento.domain.resume.service;

import com.cvmento.domain.resume.dto.response.ResumeLlmResponse;
import com.cvmento.domain.resume.client.ResumeLlmFeignClient;
import com.cvmento.domain.resume.client.ResumeLlmVisionFeignClient;
import com.cvmento.global.exception.customException.ResumeAiException;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * LLM 클라이언트 서비스 - 이력서 AI용
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ResumeLlmClientService {

    private final ResumeLlmFeignClient resumeLlmFeignClient;
    private final ResumeLlmVisionFeignClient resumeLlmVisionFeignClient;
    private final ObjectMapper objectMapper;

    /**
     * 텍스트 / 이미지 입력을 모두 처리하는 범용 메서드
     *
     * @param prompt      분석 프롬프트
     * @param base64Image Base64 인코딩 이미지 (없으면 null)
     * @param contentType 이미지 Content-Type (예: image/png, 없으면 null)
     */
    public ResumeLlmResponse analyzeResume(String prompt) {
        return analyzeUniversal(prompt, null, null);
    }

    public ResumeLlmResponse analyzeUniversal(String prompt, String base64Image, String contentType) {
        validatePrompt(prompt);

        try {
            log.info("=== LLM API 요청 시작 ===");
            log.info("프롬프트 길이: {}", prompt.length());
            if (base64Image != null) {
                log.info("이미지 데이터 길이: {}", base64Image.length());
                log.info("Content Type: {}", contentType);
            }

            // 메시지 content 생성
            List<Map<String, Object>> contentList = new ArrayList<>();

            // 텍스트 입력
            contentList.add(Map.of(
                    "type", "input_text",
                    "text", prompt
            ));

            // 이미지 입력 (옵션)
            if (base64Image != null && !base64Image.isBlank()) {
                validateBase64Image(base64Image);
                contentList.add(Map.of(
                        "type", "input_image",
                        "image_url", "data:" + contentType + ";base64," + base64Image
                ));
            }

            // 최종 요청 객체
            Map<String, Object> request = Map.of(
                    "model", "gpt-5-nano",
                    "input", List.of(Map.of(
                            "role", "user",
                            "content", contentList
                    ))
            );

            // API 호출 (이미지가 있으면 Vision, 없으면 일반 모델)
            String rawResponse = (base64Image != null && !base64Image.isBlank())
                    ? resumeLlmVisionFeignClient.analyzeVision(request)
                    : resumeLlmFeignClient.analyzeRaw(request);

            log.info("=== 원본 응답 받음 ===");

            ResumeLlmResponse response = parseOpenAiResponse(rawResponse);

            log.info("=== 변환된 응답 ===");
            log.info("응답 내용 길이: {}", response.response() != null ? response.response().length() : "null");

            return response;

        } catch (FeignException e) {
            log.error("LLM API 호출 중 Feign 오류 발생. Status: {}, Body: {}", e.status(), e.contentUTF8(), e);

            if (e.getCause() instanceof java.net.ConnectException) {
                throw new ResumeAiException("AI 서비스에 연결할 수 없습니다. 네트워크 상태를 확인하거나 잠시 후 다시 시도해주세요.", e);
            }

            throw new ResumeAiException("AI 서비스로부터 응답을 받는데 실패했습니다. (HTTP Status: " + e.status() + ")", e);

        } catch (Exception e) {
            log.error("LLM API 호출 중 알 수 없는 오류 발생", e);
            throw new ResumeAiException("AI 서비스 요청 중 알 수 없는 오류가 발생했습니다.", e);
        }
    }

    private void validatePrompt(String prompt) {
        if (prompt == null || prompt.trim().isEmpty()) {
            throw new IllegalArgumentException("프롬프트가 비어있습니다.");
        }
    }

    private void validateBase64Image(String base64Image) {
        if (base64Image == null || base64Image.trim().isEmpty()) {
            throw new IllegalArgumentException("이미지 데이터가 비어있습니다.");
        }
    }

    // OpenAI 응답 파싱
    private ResumeLlmResponse parseOpenAiResponse(String rawResponse) {
        if (rawResponse == null || rawResponse.trim().isEmpty()) {
            log.warn("응답이 비어있습니다");
            return new ResumeLlmResponse("");
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
                                        return parseActualContent(text);
                                    }
                                }
                            }
                        }
                    }
                }
            }

            log.warn("예상된 구조를 찾지 못함 - 전체 응답을 사용");
            return new ResumeLlmResponse(rawResponse);

        } catch (Exception e) {
            log.error("JSON 파싱 실패: {}", e.getMessage());
            return new ResumeLlmResponse(rawResponse);
        }
    }

    private ResumeLlmResponse parseActualContent(String text) {
        String cleanedText = text.trim();

        if (cleanedText.startsWith("```json")) {
            cleanedText = cleanedText.substring(7);
            if (cleanedText.endsWith("```")) {
                cleanedText = cleanedText.substring(0, cleanedText.length() - 3);
            }
            cleanedText = cleanedText.trim();
        } else if (!cleanedText.startsWith("{")) {
            int jsonStart = cleanedText.indexOf('{');
            if (jsonStart != -1) {
                cleanedText = cleanedText.substring(jsonStart);
            }
        }

        log.info("LLM 응답에서 추출 및 정리된 내용 길이: {} chars", cleanedText.length());
        return new ResumeLlmResponse(cleanedText);
    }
}
