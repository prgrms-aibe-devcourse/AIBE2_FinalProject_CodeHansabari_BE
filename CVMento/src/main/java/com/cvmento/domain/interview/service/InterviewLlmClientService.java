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
            log.info("=== Interview LLM API 요청 시작 ===");
            log.info("요청 프롬프트 길이: {}", request.input().length());

            String rawResponse = getRawResponse(request);
            log.info("=== 원본 응답 받음 ===");

            InterviewLlmResponse response = parseOpenAiResponse(rawResponse);

            log.info("=== 변환된 응답 ===");
            log.info("질문/답변 개수: {}", response.qnaList() != null ? response.qnaList().size() : 0);

            return response;

        } catch (Exception e) {
            log.error("Interview LLM API 호출 실패", e);
            throw new InterviewException("면접 질문/답변 생성에 실패했습니다.", e);
        }
    }

    private String getRawResponse(LlmRequest request) {
        try {
            return interviewLlmFeignClient.analyzeRaw(request);
        } catch (Exception e) {
            log.error("Raw 응답 받기 실패", e);
            throw new InterviewException("Interview LLM API 호출 실패", e);
        }
    }

    private InterviewLlmResponse parseOpenAiResponse(String rawResponse) {
        try {
            String textContent = openAiResponseParser.extractTextContent(rawResponse);
            return parseActualContent(textContent);

        } catch (Exception e) {
            log.error("OpenAI 응답 파싱 실패: {}", e.getMessage());
            throw new InterviewException("LLM 응답 파싱에 실패했습니다.", e);
        }
    }

    private InterviewLlmResponse parseActualContent(String text) {
        try {
            // 실제 LLM 응답 내용 로깅 추가
            log.info("=== LLM 실제 응답 내용 ===");
            log.info("응답 길이: {} chars", text.length());
            log.info("응답 내용: {}", text);
            log.info("============================");

            // 마크다운 코드 블록 제거 (혹시 있을 경우)
            String cleanText = text.trim()
                    .replaceAll("```json\\s*", "")
                    .replaceAll("\\s*```", "")
                    .trim();

            // text가 JSON 형식인지 확인하고 파싱
            if (cleanText.startsWith("{")) {
                var contentJson = objectMapper.readTree(cleanText);

                // qnaList 배열 추출
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

                        log.info("실제 content 파싱 성공 - QnA 개수: {}", qnaList.size());
                        return new InterviewLlmResponse(qnaList);
                    }
                } else {
                    // qnaList가 없는 경우 다른 가능한 필드명들 확인
                    log.error("qnaList 필드가 없습니다. 사용 가능한 필드들:");
                    contentJson.fieldNames().forEachRemaining(fieldName ->
                            log.error("- {}: {}", fieldName, contentJson.get(fieldName).getNodeType()));
                }
            }

            // JSON 파싱에 실패한 경우
            log.error("QnA JSON 파싱 실패 - qnaList를 찾을 수 없습니다");
            throw new InterviewException("질문/답변 데이터 파싱에 실패했습니다.");

        } catch (InterviewException e) {
            throw e; // 이미 처리된 예외는 다시 던지기
        } catch (Exception e) {
            log.error("실제 content 파싱 실패: {}", e.getMessage());
            throw new InterviewException("LLM 컨텐츠 파싱에 실패했습니다.", e);
        }
    }

    // ======================== 커스텀 프롬프트 메서드 ========================
    public CustomAnswerResponse generateCustomAnswer(String prompt) {
        validatePrompt(prompt);

        LlmRequest request = createLlmRequest(prompt);
        return callCustomAnswerLlmApi(request);
    }

    private CustomAnswerResponse callCustomAnswerLlmApi(LlmRequest request) {
        try {
            log.info("=== Custom Answer LLM API 요청 시작 ===");
            log.info("요청 모델: {}", request.model());
            log.info("요청 프롬프트 길이: {}", request.input().length());

            String rawResponse = getRawResponse(request);
            log.info("=== 원본 응답 받음 ===");

            CustomAnswerResponse response = parseCustomAnswerResponse(rawResponse);

            log.info("=== 변환된 응답 ===");
            log.info("답변 길이: {}", response.answer() != null ? response.answer().length() : 0);

            return response;

        } catch (Exception e) {
            log.error("Custom Answer LLM API 호출 실패", e);
            throw new InterviewException("커스텀 질문 답변 생성에 실패했습니다.", e);
        }
    }

    private CustomAnswerResponse parseCustomAnswerResponse(String rawResponse) {
        try {
            String textContent = openAiResponseParser.extractTextContent(rawResponse);
            return parseCustomAnswerContent(textContent);

        } catch (Exception e) {
            log.error("Custom Answer OpenAI 응답 파싱 실패: {}", e.getMessage());
            throw new InterviewException("커스텀 답변 파싱에 실패했습니다.", e);
        }
    }

    private CustomAnswerResponse parseCustomAnswerContent(String text) {
        try {
            log.info("=== Custom Answer 실제 응답 내용 ===");
            log.info("응답 길이: {} chars", text.length());
            log.info("응답 내용: {}", text);
            log.info("============================");

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

                    log.info("커스텀 답변 파싱 성공 - 답변 길이: {}, 팁 길이: {}",
                            answer.length(), tip.length());
                    return new CustomAnswerResponse(answer, tip);
                } else {
                    log.error("answer 또는 tip 필드가 없습니다. 사용 가능한 필드들:");
                    contentJson.fieldNames().forEachRemaining(fieldName ->
                            log.error("- {}: {}", fieldName, contentJson.get(fieldName).getNodeType()));
                }
            }

            log.error("커스텀 답변 JSON 파싱 실패 - answer, tip을 찾을 수 없습니다");
            throw new InterviewException("커스텀 답변 데이터 파싱에 실패했습니다.");

        } catch (InterviewException e) {
            throw e;
        } catch (Exception e) {
            log.error("커스텀 답변 content 파싱 실패: {}", e.getMessage());
            throw new InterviewException("커스텀 답변 컨텐츠 파싱에 실패했습니다.", e);
        }
    }
}