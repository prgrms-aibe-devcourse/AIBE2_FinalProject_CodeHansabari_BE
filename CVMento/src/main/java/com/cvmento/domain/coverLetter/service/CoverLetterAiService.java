package com.cvmento.domain.coverLetter.service;

import com.cvmento.domain.coverLetter.dto.internal.CoverLetterFeatureDto;
import com.cvmento.domain.coverLetter.dto.request.CoverLetterAiRequest;
import com.cvmento.domain.coverLetter.dto.request.InputItem;
import com.cvmento.domain.coverLetter.dto.response.CoverLetterAiResponse;
import com.cvmento.domain.coverLetter.dto.response.CoverLetterFeedback;
import com.cvmento.domain.coverLetter.dto.response.FeedbackItem;
import com.cvmento.domain.coverLetter.dto.response.LlmAnalysisResponse;
import com.cvmento.domain.coverLetter.entity.CoverLetterFeature;
import com.cvmento.domain.coverLetter.repository.CoverLetterFeatureRepository;
import com.cvmento.global.common.services.MetricsService;
import com.cvmento.global.exception.customException.AiInvalidRequestException;
import com.cvmento.global.exception.customException.CoverLetterAiException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class CoverLetterAiService {

    private final CoverLetterFeatureRepository coverLetterFeatureRepository;
    private final CoverLetterLlmPromptService llmPromptService;
    private final CoverLetterLlmClientService llmClientService;
    private final ObjectMapper objectMapper;
    private final MetricsService metricsService;

    /**
     * 자소서 AI 개선 메인 메서드 (경력 정보 포함)
     */
    public CoverLetterAiResponse improveCoverLetter(CoverLetterAiRequest request) {
        MDC.put("spanId", "coverletter-ai-service");

        Timer.Sample sample = metricsService.startLlmApiCallTimer();

        try {
            // 1. 특징 데이터 로드
            List<CoverLetterFeatureDto> featuresDtoList = loadCoverLetterFeatures();

            // 2. 입력 배열 생성 (Responses API 형식)
            MDC.put("spanId", "prompt-generation-service");
            List<InputItem> inputItems = llmPromptService.buildInputItems(
                    request.content(),
                    featuresDtoList,
                    request.jobField(),
                    request.getTotalExperience(),
                    request.customPrompt()
            );

            MDC.put("spanId", "coverletter-ai-service");
            log.info("자소서 AI 첨삭 처리 시작 - 지원분야: {}, 경력: {}, 특징데이터수: {}, 입력항목수: {}",
                    request.jobField(), request.getTotalExperience(),
                    featuresDtoList.size(), inputItems.size());

            // 3. LLM API 호출 (Responses API)
            LlmAnalysisResponse llmResponse = llmClientService.analyze(inputItems);

            // 4. 피드백 파싱 및 검증
            CoverLetterFeedback feedback = parseFeedback(llmResponse.feedback());

            // 5. 최종 응답 생성
            CoverLetterAiResponse result = buildResponse(feedback, llmResponse.improvedContent());

            metricsService.stopLlmApiCallTimer(sample);
            return result;

        } catch (AiInvalidRequestException e) {
            metricsService.stopLlmApiCallTimer(sample);
            metricsService.incrementErrorCount("AI_INVALID_REQUEST");
            throw e;

        } catch (Exception e) {
            metricsService.stopLlmApiCallTimer(sample);
            metricsService.incrementErrorCount("COVER_LETTER_AI_ERROR");
            logError(e, request);
            throw new CoverLetterAiException("AI 개선 처리 중 오류가 발생했습니다.", e);
        }
    }

    /** DB 에서 우수 자소서 특징 데이터를 조회 */
    private List<CoverLetterFeatureDto> loadCoverLetterFeatures() {
        MDC.put("spanId", "feature-repository");

        try {
            List<CoverLetterFeature> features = coverLetterFeatureRepository.findAll();

            MDC.put("spanId", "coverletter-ai-service");
            if (features.isEmpty()) {
                log.warn("우수 자소서 특징 데이터가 없습니다.");
                metricsService.incrementErrorCount("COVER_LETTER_FEATURES_EMPTY");
            }

            return features.stream()
                    .map(f -> new CoverLetterFeatureDto(f.getFeaturesCategory().name(), f.getDescription()))
                    .toList();
        } catch (Exception e) {
            metricsService.incrementErrorCount("COVER_LETTER_FEATURES_LOAD_ERROR");
            throw e;
        }
    }

    /** LLM API 에서 반환된 피드백 JSON을 파싱 */
    private CoverLetterFeedback parseFeedback(String feedbackJson) {
        MDC.put("spanId", "feedback-parsing-service");

        if (feedbackJson == null || feedbackJson.trim().isEmpty()) {
            log.warn("피드백 JSON이 비어있음");
            metricsService.incrementErrorCount("FEEDBACK_JSON_EMPTY");
            return createDefaultFeedback();
        }

        try {
            var feedback = objectMapper.readValue(feedbackJson, CoverLetterFeedback.class);
            log.debug("피드백 파싱 성공 - 강점: {}개, 개선사항: {}개",
                    feedback.strengths() != null ? feedback.strengths().size() : 0,
                    feedback.improvements() != null ? feedback.improvements().size() : 0);
            return validateAndCorrectFeedback(feedback);
        } catch (JsonProcessingException e) {
            log.error("피드백 JSON 파싱 실패 - 길이: {}", feedbackJson.length(), e);
            metricsService.incrementErrorCount("FEEDBACK_JSON_PARSE_ERROR");
            return createDefaultFeedback();
        }
    }

    /** 파싱된 피드백 객체를 검증하고 필드 보정 */
    private CoverLetterFeedback validateAndCorrectFeedback(CoverLetterFeedback feedback) {
        List<FeedbackItem> validStrengths = validateFeedbackItems(feedback.strengths());
        List<FeedbackItem> validImprovements = validateFeedbackItems(feedback.improvements());
        String validSummary = feedback.summary() != null ? feedback.summary() : "분석 완료";

        return new CoverLetterFeedback(validStrengths, validImprovements, validSummary);
    }

    /** 피드백 리스트에서 null 제거 및 유효 항목 필터링 */
    private List<FeedbackItem> validateFeedbackItems(List<FeedbackItem> items) {
        if (items == null) return List.of();
        return items.stream()
                .filter(Objects::nonNull)
                .filter(this::isValidFeedbackItem)
                .toList();
    }

    /** 파싱 실패 시 기본 피드백 반환 */
    private CoverLetterFeedback createDefaultFeedback() {
        return new CoverLetterFeedback(
                List.of(),
                List.of(),
                "분석 결과를 불러오는 중 오류가 발생했습니다. 다시 시도해주세요."
        );
    }

    /** 최종 응답 객체 생성 및 개선 내용 검증 */
    private CoverLetterAiResponse buildResponse(CoverLetterFeedback feedback, String improvedContent) {
        if (improvedContent == null || improvedContent.trim().isEmpty()) {
            log.warn("개선된 자소서 내용이 비어있습니다.");
            metricsService.incrementErrorCount("IMPROVED_CONTENT_EMPTY");
            improvedContent = "개선된 내용을 생성하는 중 오류가 발생했습니다.";
        }
        return new CoverLetterAiResponse(feedback, improvedContent.trim());
    }

    /** 서비스 수행 중 발생한 에러를 로깅 */
    private void logError(Exception e, CoverLetterAiRequest request) {
        log.error("자소서 AI 개선 중 오류 발생 - content length: {}, jobField: {}, experience: {}, error: {}",
                request != null ? request.content().length() : 0,
                request != null ? request.jobField() : "없음",
                request != null ? request.getTotalExperience() : "없음",
                e.getMessage(), e);
    }

    private boolean isValidFeedbackItem(FeedbackItem item) {
        if (item == null) return false;

        return item.description() != null && !item.description().trim().isEmpty()
                && item.suggestion() != null && !item.suggestion().trim().isEmpty();
    }
}