package com.cvmento.domain.resume.service;

import com.cvmento.domain.resume.client.ResumeLlmFeignClient;
import com.cvmento.domain.resume.dto.request.ResumeLlmRequest;
import com.cvmento.domain.resume.dto.request.ResumeVisionRequest;
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
    
    public ResumeImportResponse convertResumeWithVision(String textPrompt, String base64Image) {
        MDC.put("spanId", "resume-llm-client");

        validatePrompt(textPrompt);
        validateBase64Image(base64Image);
        ResumeVisionRequest request = createVisionRequest(textPrompt, base64Image);
        return callVisionApi(request);
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

    private ResumeImportResponse parseVisionApiResponse(String rawResponse) {
        try {
            MDC.put("spanId", "openai-response-parser");
            String textContent = extractVisionContent(rawResponse);

            MDC.put("spanId", "resume-response-parsing");
            return parseActualContent(textContent);

        } catch (Exception e) {
            log.error("Vision API 응답 파싱 실패: {}", e.getMessage());
            throw new ResumeException("Vision API 응답 파싱에 실패했습니다.", e);
        }
    }

    private String extractVisionContent(String rawResponse) {
        try {
            log.info("Vision API 원본 응답: {}", rawResponse);
            
            // /chat/completions 응답에서 content 추출
            com.fasterxml.jackson.databind.JsonNode responseNode = objectMapper.readTree(rawResponse);
            
            if (responseNode.has("choices") && responseNode.get("choices").isArray()) {
                com.fasterxml.jackson.databind.JsonNode firstChoice = responseNode.get("choices").get(0);
                if (firstChoice.has("message") && firstChoice.get("message").has("content")) {
                    String content = firstChoice.get("message").get("content").asText();
                    log.info("Vision API에서 추출한 content: {}", content);
                    return content;
                }
            }
            
            log.warn("Vision API 응답에서 content를 찾을 수 없음");
            return rawResponse; // fallback으로 전체 응답 반환
            
        } catch (Exception e) {
            log.error("Vision content 추출 실패: {}", e.getMessage());
            return rawResponse; // fallback
        }
    }

    private ResumeImportResponse parseActualContent(String text) {
        try {
            log.info("Resume 응답 파싱 시작 - 텍스트길이: {}", text.length());
            log.info("원본 응답 텍스트: {}", text);

            // 마크다운 코드 블록 제거
            String cleanText = text.trim()
                    .replaceAll("```json\\s*", "")
                    .replaceAll("```\\s*json\\s*", "")
                    .replaceAll("\\s*```", "")
                    .trim();

            log.info("정제된 텍스트: {}", cleanText);

            // JSON 찾기 시도 - { 로 시작하는 부분 찾기
            int jsonStart = cleanText.indexOf("{");
            if (jsonStart != -1) {
                // 마지막 } 찾기
                int jsonEnd = cleanText.lastIndexOf("}");
                if (jsonEnd != -1 && jsonEnd > jsonStart) {
                    String jsonText = cleanText.substring(jsonStart, jsonEnd + 1);
                    log.info("추출된 JSON: {}", jsonText);
                    
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

    private void validateBase64Image(String base64Image) {
        if (base64Image == null || base64Image.trim().isEmpty()) {
            throw new IllegalArgumentException("Base64 이미지 데이터가 비어있습니다.");
        }
        if (!base64Image.startsWith("data:")) {
            throw new IllegalArgumentException("올바르지 않은 Base64 이미지 형식입니다.");
        }
    }

    private ResumeVisionRequest createVisionRequest(String textPrompt, String base64Image) {
        return ResumeVisionRequest.create("gpt-5-nano", textPrompt, base64Image);
    }

    private ResumeImportResponse callVisionApi(ResumeVisionRequest request) {
        try {
            log.info("Resume Vision API 요청 시작 - 모델: {}", request.model());

            MDC.put("spanId", "openai-resume-api");
            String rawResponse = getVisionRawResponse(request);

            MDC.put("spanId", "resume-response-parsing");
            log.info("Resume Vision 원본 응답 수신 완료 - 응답길이: {}", rawResponse.length());

            ResumeImportResponse response = parseVisionApiResponse(rawResponse);

            MDC.put("spanId", "resume-llm-client");
            log.info("Resume Vision 응답 파싱 완료 - 이름: {}, 제목: {}",
                    response.name(), response.title());

            return response;

        } catch (Exception e) {
            log.error("Resume Vision API 호출 실패: {}", e.getMessage(), e);
            throw new ResumeException("이력서 이미지 변환에 실패했습니다.", e);
        }
    }

    private String getVisionRawResponse(ResumeVisionRequest request) {
        try {
            // 요청 JSON 디버깅을 위한 로그
            try {
                String requestJson = objectMapper.writeValueAsString(request);
                // Base64는 너무 길어서 앞부분만 로그
                String shortenedJson = requestJson.length() > 1000 ? 
                    requestJson.substring(0, 1000) + "..." : requestJson;
                log.info("Vision API 요청 JSON: {}", shortenedJson);
            } catch (Exception jsonEx) {
                log.warn("요청 JSON 로깅 실패: {}", jsonEx.getMessage());
            }
            
            return resumeLlmFeignClient.analyzeVision(request);
        } catch (Exception e) {
            log.error("Vision API 원본 응답 받기 실패: {}", e.getMessage());
            throw new ResumeException("Resume Vision API 호출 실패", e);
        }
    }

    private ResumeImportResponse createDefaultResponse(String originalText) {
        log.warn("기본 이력서 응답 생성 - LLM JSON 파싱 실패로 인한 fallback");
        log.warn("원본 LLM 응답 전체: {}", originalText);
        
        return new ResumeImportResponse(
                "변환된 이력서 (자동생성)",
                com.cvmento.domain.resume.enums.ResumeType.DEFAULT,
                "홍길동",
                "sample@example.com",
                1990,
                "010-1234-5678",
                com.cvmento.domain.resume.enums.CareerType.FRESHMAN,
                "소프트웨어 개발자",
                "LLM 응답을 파싱할 수 없어 기본값으로 생성된 이력서입니다. 원본 응답: " + 
                (originalText.length() > 200 ? originalText.substring(0, 200) + "..." : originalText),
                null, null, null,
                java.util.List.of(), java.util.List.of(), java.util.List.of(),
                java.util.List.of(), java.util.List.of(), java.util.List.of(),
                java.util.List.of()
        );
    }
}