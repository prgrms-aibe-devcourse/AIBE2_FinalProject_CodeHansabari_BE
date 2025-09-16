package com.cvmento.domain.interview.service;

import com.cvmento.domain.coverLetter.dto.request.LlmRequest;
import com.cvmento.domain.coverLetter.dto.request.InputItem;
import com.cvmento.domain.interview.client.InterviewLlmFeignClient;
import com.cvmento.domain.interview.dto.response.CustomAnswerResponse;
import com.cvmento.domain.interview.dto.response.InterviewLlmResponse;
import com.cvmento.domain.interview.dto.response.InterviewQnaDto;
import com.cvmento.global.common.util.OpenAiResponseParser;
import com.cvmento.global.exception.customException.AiInvalidRequestException;
import com.cvmento.global.exception.customException.InterviewException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.slf4j.MDC;
import com.cvmento.global.common.util.LlmParsingUtil;

import java.util.ArrayList;
import java.util.List;

/** 인터뷰 LLM 호출/파싱 서비스 */
@Service
@RequiredArgsConstructor
@Slf4j
public class InterviewLlmClientService {

    private final InterviewLlmFeignClient interviewLlmFeignClient;
    private final ObjectMapper objectMapper;
    private final OpenAiResponseParser openAiResponseParser;

    /**
     * Q&A 리스트 생성 (Responses API)
     * 자소서 기반 자동 생성이므로 별도 검증 불필요
     */
    public InterviewLlmResponse generateQnaList(List<InputItem> inputItems) {
        MDC.put("spanId", "interview-llm-client");

        validateInputItems(inputItems);
        LlmRequest request = createLlmRequest(inputItems);
        return callQnaLlmApi(request);
    }

    /**
     * 커스텀 답변 생성 (Responses API)
     * 사용자 입력 기반이므로 검증 필요
     */
    public CustomAnswerResponse generateCustomAnswer(List<InputItem> inputItems) {
        MDC.put("spanId", "interview-llm-client");

        validateInputItems(inputItems);
        LlmRequest request = createLlmRequest(inputItems);
        return callCustomAnswerLlmApi(request);
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

    private InterviewLlmResponse callQnaLlmApi(LlmRequest request) {
        try {
            log.info("Interview Q&A LLM API 요청 시작 - 모델: {}, 입력항목수: {}",
                    request.model(), request.input().size());

            MDC.put("spanId", "openai-interview-api");
            String rawResponse = getRawResponse(request);

            MDC.put("spanId", "interview-response-parsing");
            log.info("Interview Q&A 원본 응답 수신 완료 - 응답길이: {}", rawResponse.length());

            InterviewLlmResponse response = parseQnaResponse(rawResponse);

            MDC.put("spanId", "interview-llm-client");
            log.info("Interview Q&A 응답 파싱 완료 - Q&A 개수: {}",
                    response.qnaList() != null ? response.qnaList().size() : 0);

            return response;

        } catch (Exception e) {
            log.error("Interview Q&A LLM API 호출 실패 - 모델: {}, 오류: {}", request.model(), e.getMessage(), e);
            throw new InterviewException("면접 질문/답변 생성에 실패했습니다.", e);
        }
    }

    private CustomAnswerResponse callCustomAnswerLlmApi(LlmRequest request) {
        try {
            log.info("Custom Answer LLM API 요청 시작 - 모델: {}, 입력항목수: {}",
                    request.model(), request.input().size());

            MDC.put("spanId", "openai-interview-api");
            String rawResponse = getRawResponse(request);

            MDC.put("spanId", "custom-answer-parsing");
            log.info("Custom Answer 원본 응답 수신 완료 - 응답길이: {}", rawResponse.length());

            CustomAnswerResponse response = parseCustomAnswerResponse(rawResponse);

            MDC.put("spanId", "interview-llm-client");
            log.info("Custom Answer 파싱 완료 - 답변길이: {}",
                    response.answer() != null ? response.answer().length() : 0);

            // 커스텀 답변은 사용자 입력 기반이므로 검증 필요
            validateCustomAnswerResponse(response);

            return response;

        } catch (AiInvalidRequestException e) {
            log.warn("부적절한 커스텀 답변 요청 감지 - 모델: {}, 오류: {}", request.model(), e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Custom Answer LLM API 호출 실패 - 모델: {}, 오류: {}", request.model(), e.getMessage(), e);
            throw new InterviewException("커스텀 질문 답변 생성에 실패했습니다.", e);
        }
    }

    /**
     * 커스텀 답변 응답 검증 - 거절/품질 미달 체크
     */
    private void validateCustomAnswerResponse(CustomAnswerResponse response) {
        if (isInvalidCustomAnswerResponse(response)) {
            log.warn("부적절한 커스텀 답변 응답 감지 - 거절 또는 품질 미달");
            throw new AiInvalidRequestException("면접과 관련되지 않은 요청입니다. 면접 준비와 관련된 요청만 가능합니다.");
        }
    }

    /**
     * 커스텀 답변 응답이 유효하지 않은지 통합 판단
     */
    private boolean isInvalidCustomAnswerResponse(CustomAnswerResponse response) {
        // 1. 품질 미달 체크
        if (response.answer() == null || response.answer().trim().length() < 50) {
            log.debug("커스텀 답변이 너무 짧거나 비어있음");
            return true;
        }

        if (response.tip() == null || response.tip().trim().isEmpty()) {
            log.debug("커스텀 팁이 비어있음");
            return true;
        }

        // 2. 거절 키워드 체크
        if (containsRejectionKeywords("", response.answer(), response.tip())) {
            log.debug("커스텀 답변에서 거절 키워드 감지");
            return true;
        }

        return false;
    }

    /**
     * 거절 키워드가 포함되어 있는지 확인
     */
    private boolean containsRejectionKeywords(String question, String answer, String tip) {
        String[] rejectionKeywords = {
                "면접 준비 서비스만",
                "면접과 관련되지 않은",
                "면접 준비와 관련되지",
                "제공할 수 없습니다",
                "거절합니다",
                "관련되지 않은 요청",
                "면접과 무관한"
        };

        // 각 필드에서 거절 키워드 체크
        String[] fieldsToCheck = {question, answer, tip};

        for (String field : fieldsToCheck) {
            if (field != null) {
                String lowerField = field.toLowerCase();
                for (String keyword : rejectionKeywords) {
                    if (lowerField.contains(keyword.toLowerCase())) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    private String getRawResponse(LlmRequest request) {
        try {
            return interviewLlmFeignClient.analyzeRaw(request);
        } catch (Exception e) {
            log.error("LLM API 원본 응답 받기 실패: {}", e.getMessage());
            throw new InterviewException("Interview LLM API 호출 실패", e);
        }
    }

    private InterviewLlmResponse parseQnaResponse(String rawResponse) {
        try {
            MDC.put("spanId", "openai-response-parser");
            String textContent = openAiResponseParser.extractTextContent(rawResponse);

            MDC.put("spanId", "interview-response-parsing");
            return parseQnaContent(textContent);

        } catch (Exception e) {
            log.error("Interview Q&A OpenAI 응답 파싱 실패: {}", e.getMessage());
            throw new InterviewException("Q&A 응답 파싱에 실패했습니다.", e);
        }
    }

    private CustomAnswerResponse parseCustomAnswerResponse(String rawResponse) {
        try {
            MDC.put("spanId", "openai-response-parser");
            String textContent = openAiResponseParser.extractTextContent(rawResponse);

            MDC.put("spanId", "custom-answer-parsing");
            return parseCustomAnswerContent(textContent);

        } catch (Exception e) {
            log.error("Custom Answer OpenAI 응답 파싱 실패: {}", e.getMessage());
            throw new InterviewException("커스텀 답변 파싱에 실패했습니다.", e);
        }
    }

    /**
     * Q&A 컨텐츠 파싱 (마크다운 코드 블록 처리 포함)
     */
    private InterviewLlmResponse parseQnaContent(String text) {
        try {
            // 1. 마크다운 코드 블록 제거
            String cleanedText = LlmParsingUtil.removeMarkdownCodeBlocks(text);

            log.debug("Q&A 텍스트 정제 완료 - 원본: {}chars, 정제후: {}chars",
                    text.length(), cleanedText.length());

            // 2. JSON 파싱 시도
            if (cleanedText.startsWith("{")) {
                var contentJson = objectMapper.readTree(cleanedText);

                if (contentJson.has("qnaList")) {
                    var qnaArray = contentJson.get("qnaList");
                    if (qnaArray.isArray()) {
                        List<InterviewQnaDto> qnaList = new ArrayList<>();

                        for (var qnaNode : qnaArray) {
                            if (qnaNode.has("question") && qnaNode.has("answer") && qnaNode.has("tip")) {
                                String question = qnaNode.get("question").asText();
                                String answer = qnaNode.get("answer").asText();
                                String tip = qnaNode.get("tip").asText();
                                qnaList.add(new InterviewQnaDto(question, answer, tip));
                            }
                        }

                        log.info("Interview Q&A 파싱 성공 - 개수: {}", qnaList.size());
                        return new InterviewLlmResponse(qnaList);
                    }
                } else {
                    log.error("qnaList 필드가 없습니다. 사용 가능한 필드들:");
                    contentJson.fieldNames().forEachRemaining(fieldName ->
                            log.error("- {}: {}", fieldName, contentJson.get(fieldName).getNodeType()));
                }
            }

            log.error("Interview Q&A JSON 파싱 실패 - qnaList를 찾을 수 없습니다");
            throw new InterviewException("질문/답변 데이터 파싱에 실패했습니다.");

        } catch (InterviewException e) {
            throw e;
        } catch (Exception e) {
            log.error("Interview Q&A content 파싱 실패: {}", e.getMessage());
            throw new InterviewException("Q&A 컨텐츠 파싱에 실패했습니다.", e);
        }
    }

    /**
     * 커스텀 답변 컨텐츠 파싱 (마크다운 코드 블록 처리 포함)
     */
    private CustomAnswerResponse parseCustomAnswerContent(String text) {
        try {
            // 1. 마크다운 코드 블록 제거
            String cleanedText = LlmParsingUtil.removeMarkdownCodeBlocks(text);

            log.debug("Custom Answer 텍스트 정제 완료 - 원본: {}chars, 정제후: {}chars",
                    text.length(), cleanedText.length());

            // 2. JSON 파싱 시도
            if (cleanedText.startsWith("{")) {
                var contentJson = objectMapper.readTree(cleanedText);

                if (contentJson.has("answer") && contentJson.has("tip")) {
                    String answer = contentJson.get("answer").asText();
                    String tip = contentJson.get("tip").asText();

                    log.info("Custom Answer 파싱 성공 - 답변길이: {}, 팁길이: {}",
                            answer.length(), tip.length());
                    return new CustomAnswerResponse(answer, tip);
                } else {
                    log.error("answer 또는 tip 필드가 없습니다. 사용 가능한 필드들:");
                    contentJson.fieldNames().forEachRemaining(fieldName ->
                            log.error("- {}: {}", fieldName, contentJson.get(fieldName).getNodeType()));
                }
            }

            log.error("Custom Answer JSON 파싱 실패 - answer, tip을 찾을 수 없습니다");
            throw new InterviewException("커스텀 답변 데이터 파싱에 실패했습니다.");

        } catch (InterviewException e) {
            throw e;
        } catch (Exception e) {
            log.error("Custom Answer content 파싱 실패: {}", e.getMessage());
            throw new InterviewException("커스텀 답변 컨텐츠 파싱에 실패했습니다.", e);
        }
    }
}