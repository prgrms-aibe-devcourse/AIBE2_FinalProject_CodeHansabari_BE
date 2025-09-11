package com.cvmento.domain.interview.service;

import com.cvmento.domain.coverLetter.dto.request.LlmRequest;
import com.cvmento.domain.interview.client.InterviewLlmFeignClient;
import com.cvmento.domain.interview.dto.response.CustomAnswerResponse;
import com.cvmento.domain.interview.dto.response.InterviewLlmResponse;
import com.cvmento.domain.interview.dto.response.InterviewQnaDto;
import com.cvmento.global.common.util.OpenAiResponseParser;
import com.cvmento.global.exception.customException.InterviewException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import org.slf4j.MDC;

/**
 * Interview LLM 클라이언트 서비스 - 구조화된 파싱
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class InterviewLlmClientService {

    private final InterviewLlmFeignClient interviewLlmFeignClient;
    private final ObjectMapper objectMapper;
    private final OpenAiResponseParser openAiResponseParser;

    public InterviewLlmResponse generateQnaList(String prompt) {
        MDC.put("spanId", "interview-llm-client");

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

    private InterviewLlmResponse callLlmApi(LlmRequest request) {
        try {
            log.info("Interview LLM API 요청 시작 - 프롬프트길이: {}", request.input().length());

            MDC.put("spanId", "openai-interview-api");
            String rawResponse = getRawResponse(request);

            MDC.put("spanId", "interview-response-parsing");
            log.info("Interview LLM 원본 응답 수신 완료 - 응답길이: {}", rawResponse.length());

            InterviewLlmResponse response = parseOpenAiResponse(rawResponse);

            MDC.put("spanId", "interview-llm-client");
            log.info("Interview 응답 파싱 완료 - Q&A 개수: {}",
                    response.qnaList() != null ? response.qnaList().size() : 0);

            return response;

        } catch (Exception e) {
            log.error("Interview LLM API 호출 실패: {}", e.getMessage(), e);
            throw new InterviewException("면접 질문/답변 생성에 실패했습니다.", e);
        }
    }

    private String getRawResponse(LlmRequest request) {
        try {
            return interviewLlmFeignClient.analyzeRaw(request);
        } catch (Exception e) {
            log.error("LLM API 원본 응답 받기 실패: {}", e.getMessage());
            throw new InterviewException("Interview LLM API 호출 실패", e);
        }
    }

    private InterviewLlmResponse parseOpenAiResponse(String rawResponse) {
        try {
            MDC.put("spanId", "openai-response-parser");
            String textContent = openAiResponseParser.extractTextContent(rawResponse);

            MDC.put("spanId", "interview-response-parsing");
            return parseActualContent(textContent);

        } catch (Exception e) {
            log.error("Interview OpenAI 응답 파싱 실패: {}", e.getMessage());
            throw new InterviewException("LLM 응답 파싱에 실패했습니다.", e);
        }
    }

    private InterviewLlmResponse parseActualContent(String text) {
        try {
            log.debug("Interview 응답 파싱 시작 - 텍스트길이: {}", text.length());

            // 마크다운 코드 블록 제거
            String cleanText = text.trim()
                    .replaceAll("```json\\s*", "")
                    .replaceAll("\\s*```", "")
                    .trim();

            // JSON 파싱
            if (cleanText.startsWith("{")) {
                var contentJson = objectMapper.readTree(cleanText);

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
            log.error("Interview content 파싱 실패: {}", e.getMessage());
            throw new InterviewException("LLM 컨텐츠 파싱에 실패했습니다.", e);
        }
    }

    // ======================== 커스텀 프롬프트 메서드 ========================
    public CustomAnswerResponse generateCustomAnswer(String prompt) {
        MDC.put("spanId", "interview-llm-client");

        validatePrompt(prompt);
        LlmRequest request = createLlmRequest(prompt);
        return callCustomAnswerLlmApi(request);
    }

    private CustomAnswerResponse callCustomAnswerLlmApi(LlmRequest request) {
        try {
            log.info("Custom Answer LLM API 요청 시작 - 프롬프트길이: {}", request.input().length());

            MDC.put("spanId", "openai-interview-api");
            String rawResponse = getRawResponse(request);

            MDC.put("spanId", "custom-answer-parsing");
            log.info("Custom Answer 원본 응답 수신 완료 - 응답길이: {}", rawResponse.length());

            CustomAnswerResponse response = parseCustomAnswerResponse(rawResponse);

            MDC.put("spanId", "interview-llm-client");
            log.info("Custom Answer 파싱 완료 - 답변길이: {}",
                    response.answer() != null ? response.answer().length() : 0);

            return response;

        } catch (Exception e) {
            log.error("Custom Answer LLM API 호출 실패: {}", e.getMessage(), e);
            throw new InterviewException("커스텀 질문 답변 생성에 실패했습니다.", e);
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

    private CustomAnswerResponse parseCustomAnswerContent(String text) {
        try {
            log.debug("Custom Answer 파싱 시작 - 텍스트길이: {}", text.length());

            // 마크다운 코드 블록 제거
            String cleanText = text.trim()
                    .replaceAll("```json\\s*", "")
                    .replaceAll("\\s*```", "")
                    .trim();

            // JSON 파싱
            if (cleanText.startsWith("{")) {
                var contentJson = objectMapper.readTree(cleanText);

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