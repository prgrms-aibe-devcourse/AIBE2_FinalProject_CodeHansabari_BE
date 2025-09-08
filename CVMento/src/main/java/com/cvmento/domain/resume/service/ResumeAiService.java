package com.cvmento.domain.resume.service;

import com.cvmento.domain.resume.dto.request.ResumeLlmRequest;
import com.cvmento.domain.resume.dto.response.ResumeLlmResponse;
import com.cvmento.domain.resume.dto.request.ResumeAiExperienceRequest;
import com.cvmento.domain.resume.dto.response.ResumeAiSuggestionResponse;
import com.cvmento.domain.resume.dto.response.SuggestedResumeSectionDto;
import com.cvmento.global.exception.customException.ResumeAiException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ResumeAiService {

    private final ResumeLlmPromptService llmPromptService;
    private final ResumeLlmClientService llmClientService;
    private final ObjectMapper objectMapper;

    /**
     * 이력서 AI 제안 메인 메서드
     */
    public ResumeAiSuggestionResponse getResumeSuggestions(ResumeAiExperienceRequest request, String userEmail) {
        try {
            // 1. LLM 프롬프트 생성
            String prompt = llmPromptService.buildSuggestionPrompt(request.experienceContent());

            // 2. LLM API 호출
            ResumeLlmResponse llmResponse = llmClientService.analyzeResume(prompt);

            // 3. 제안 파싱 및 검증
            ResumeAiSuggestionResponse suggestions = parseSuggestions(llmResponse.response());

            // 4. 최종 응답 생성
            return suggestions;

        } catch (ResumeAiException e) {
            log.error("AI 서비스 처리 중 예외 발생: {}", e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            // 그 외의 모든 예외(예: NullPointerException 등)는 여기서 새로운 예외로 감싸서 던집니다.
            log.error("이력서 AI 제안 처리 중 예측하지 못한 오류 발생 - experienceContent length: {}, error: {}",
                    request != null ? request.experienceContent().length() : 0,
                    e.getMessage(), e);
            throw new ResumeAiException("이력서 AI 서비스 처리 중 오류가 발생했습니다.", e);
        }
    }

    /** LLM API에서 반환된 제안 JSON을 파싱 */
    private ResumeAiSuggestionResponse parseSuggestions(String suggestionsJson) {
        String trimmedJson = (suggestionsJson != null) ? suggestionsJson.trim() : "";

        // AI 응답이 비어있거나 유효한 JSON 객체 형식({ ... })이 아닌 경우, 예외를 발생시켜 사용자에게 알립니다.
        if (trimmedJson.isEmpty() || !trimmedJson.startsWith("{") || !trimmedJson.endsWith("}")) {
            log.warn("AI 응답이 유효한 JSON 형식이 아닙니다. 응답 내용: {}", suggestionsJson);
            throw new ResumeAiException("AI가 유효한 제안을 생성하지 못했습니다. 입력 내용을 수정하여 다시 시도해주세요.");
        }

        try {
            // Assuming the LLM returns a JSON that directly maps to ResumeAiSuggestionResponse
            return objectMapper.readValue(trimmedJson, ResumeAiSuggestionResponse.class);
        } catch (JsonProcessingException e) {
            log.error("제안 JSON 파싱 실패: {}", trimmedJson, e);
            // JSON 형식은 올바르나, 내부 구조가 DTO와 맞지 않거나 기타 파싱 오류 발생 시 예외 처리
            throw new ResumeAiException("AI 응답을 처리하는 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.", e);
        }
    }
}
