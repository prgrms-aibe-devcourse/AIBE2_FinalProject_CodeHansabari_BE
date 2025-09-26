package com.cvmento.domain.coverLetter.service;

import com.cvmento.domain.coverLetter.dto.request.LlmRequest;
import com.cvmento.domain.coverLetter.dto.request.InputItem;
import com.cvmento.domain.coverLetter.dto.response.LlmAnalysisResponse;
import com.cvmento.domain.coverLetter.client.CoverLetterLlmFeignClient;
import com.cvmento.global.common.services.MetricsService;
import com.cvmento.global.common.util.OpenAiResponseParser;
import com.cvmento.global.exception.customException.AiInvalidRequestException;
import com.cvmento.global.exception.customException.CoverLetterAiException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import com.cvmento.global.common.util.LlmParsingUtil;

import java.util.List;

/**
 * LLM 클라이언트 서비스.
 * - 요청 유효성 점검
 * - LLM 호출
 * - 응답 파싱
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CoverLetterLlmClientService {

    private final CoverLetterLlmFeignClient coverLetterLlmFeignClient;
    private final ObjectMapper objectMapper;
    private final OpenAiResponseParser openAiResponseParser;
    private final MetricsService metricsService;

    /**
     * 입력 배열을 분석 요청하고 결과를 파싱한다 (Responses API)
     *
     * @param inputItems 시스템/유저 입력 배열
     * @return 피드백/개선본문
     */
    public LlmAnalysisResponse analyze(List<InputItem> inputItems) {
        MDC.put("spanId", "llm-client-service");

        try {
            validateInputItems(inputItems);
            LlmRequest request = createLlmRequest(inputItems);
            return callLlmApi(request);
        } catch (IllegalArgumentException e) {
            metricsService.incrementErrorCount("LLM_INVALID_INPUT");
            throw e;
        }
    }

    private void validateInputItems(List<InputItem> inputItems) {
        if (inputItems == null || inputItems.isEmpty()) {
            throw new IllegalArgumentException("입력 배열이 비어있습니다.");
        }
    }

    private LlmRequest createLlmRequest(List<InputItem> inputItems) {
        return new LlmRequest(
                "gpt-5",
                inputItems
        );
    }

    private LlmAnalysisResponse callLlmApi(LlmRequest request) {
        try {
            log.info("LLM API 요청 시작 - 모델: {}, 입력항목수: {}",
                    request.model(), request.input().size());

            MDC.put("spanId", "openai-llm-api");
            String rawResponse = getRawResponse(request);

            LlmAnalysisResponse response = parseResponse(rawResponse);

            MDC.put("spanId", "llm-client-service");
            log.info("LLM 응답 파싱 완료");

            validateResponse(response);

            return response;

        } catch (AiInvalidRequestException e) {
            log.warn("부적절한 요청 감지 - {}", e.getMessage());
            metricsService.incrementErrorCount("LLM_INVALID_REQUEST_DETECTED");
            throw e;
        } catch (Exception e) {
            log.error("LLM API 호출 실패 - 모델: {}, 오류: {}", request.model(), e.getMessage(), e);
            metricsService.incrementErrorCount("LLM_API_CALL_FAILED");
            throw new CoverLetterAiException("LLM 서비스 호출에 실패했습니다.", e);
        }
    }

    /**
     * 응답 검증 - 거절/품질 미달 체크 (단일 책임)
     */
    private void validateResponse(LlmAnalysisResponse response) {
        if (isInvalidResponse(response)) {
            log.warn("부적절한 응답 감지 - 거절 또는 품질 미달");
            metricsService.incrementErrorCount("LLM_RESPONSE_INVALID");
            throw new AiInvalidRequestException(
                    "AI 요청이 잘못되었습니다. 자소서 내용과 관련된 요청만 가능합니다."
            );
        }
    }

    /**
     * 응답이 유효하지 않은지 통합 판단
     */
    private boolean isInvalidResponse(LlmAnalysisResponse response) {
        // 1. 품질 미달 체크
        if (isLowQualityResponse(response)) {
            log.debug("품질 미달 응답 감지");
            metricsService.incrementErrorCount("LLM_RESPONSE_LOW_QUALITY");
            return true;
        }

        // 2. 거절 키워드 체크 (feedback과 improvedContent 모두)
        if (containsRejectionKeywords(response)) {
            log.debug("거절 키워드 감지");
            metricsService.incrementErrorCount("LLM_RESPONSE_REJECTED");
            return true;
        }

        return false;
    }

    /**
     * 응답 품질이 낮은지 판단
     */
    private boolean isLowQualityResponse(LlmAnalysisResponse response) {
        // 1. 개선된 내용이 비어있거나 너무 짧음
        if (response.improvedContent() == null ||
                response.improvedContent().trim().length() < 50) {
            return true;
        }

        // 2. 피드백이 완전히 비어있음
        return response.feedback() == null || response.feedback().trim().isEmpty();
    }

    /**
     * 거절 키워드가 포함되어 있는지 확인 (feedback.summary 우선 체크)
     */
    private boolean containsRejectionKeywords(LlmAnalysisResponse response) {
        String[] rejectionKeywords = {
                "자소서 첨삭 서비스만",
                "자소서와 관련되지 않은",
                "자소서 첨삭과 관련되지",
                "제공할 수 없습니다",
                "거절합니다",
                "관련되지 않은 요청",
                "자소서와 무관한"
        };

        // feedback.summary 우선 체크
        if (response.feedback() != null) {
            try {
                var feedbackJson = objectMapper.readTree(response.feedback());
                if (feedbackJson.has("summary")) {
                    String summary = feedbackJson.get("summary").asText().toLowerCase();
                    for (String keyword : rejectionKeywords) {
                        if (summary.contains(keyword.toLowerCase())) {
                            return true;
                        }
                    }
                }
            } catch (Exception e) {
                // JSON 파싱 실패 시 전체 feedback 문자열에서 검색 (기존 로직)
                String lowerFeedback = response.feedback().toLowerCase();
                for (String keyword : rejectionKeywords) {
                    if (lowerFeedback.contains(keyword.toLowerCase())) {
                        return true;
                    }
                }
            }
        }

        // improvedContent 체크
        if (response.improvedContent() != null) {
            String lowerContent = response.improvedContent().toLowerCase();
            for (String keyword : rejectionKeywords) {
                if (lowerContent.contains(keyword.toLowerCase())) {
                    return true;
                }
            }
        }

        return false;
    }

    private String getRawResponse(LlmRequest request) {
        try {
            return coverLetterLlmFeignClient.analyzeRaw(request);
        } catch (Exception e) {
            log.error("LLM API 원본 응답 받기 실패: {}", e.getMessage());
            metricsService.incrementErrorCount("LLM_RAW_RESPONSE_FAILED");
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
            metricsService.incrementErrorCount("LLM_RESPONSE_PARSE_FAILED");
            throw new CoverLetterAiException("LLM 응답 파싱에 실패했습니다.", e);
        }
    }

    /**
     * 실제 컨텐츠 파싱 (단순히 파싱만 담당, 검증은 별도 수행)
     */
    private LlmAnalysisResponse parseActualContent(String text) {
        try {
            // 1. 마크다운 코드 블록 제거
            String cleanedText = LlmParsingUtil.removeMarkdownCodeBlocks(text);

            log.debug("텍스트 정제 완료 - 원본: {}chars, 정제후: {}chars",
                    text.length(), cleanedText.length());

            // 2. JSON 파싱 시도
            if (cleanedText.startsWith("{")) {
                var contentJson = objectMapper.readTree(cleanedText);

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

                log.info("JSON 파싱 성공");

                return new LlmAnalysisResponse(feedback, improvedContent);
            } else {
                // JSON이 아닌 경우 전체를 improvedContent로 사용
                log.warn("JSON 형식이 아닌 응답 - 전체를 improvedContent로 처리");
                metricsService.incrementErrorCount("LLM_RESPONSE_NON_JSON");
                return new LlmAnalysisResponse("", cleanedText);
            }
        } catch (Exception e) {
            log.error("실제 content 파싱 실패 - 텍스트길이: {}, 오류: {}",
                    text.length(), e.getMessage());
            metricsService.incrementErrorCount("LLM_CONTENT_PARSE_FAILED");
            return new LlmAnalysisResponse("", text);
        }
    }
}