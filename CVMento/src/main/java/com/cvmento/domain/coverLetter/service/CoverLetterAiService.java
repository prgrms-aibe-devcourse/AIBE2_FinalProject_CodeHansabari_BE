package com.cvmento.domain.coverLetter.service;

import com.cvmento.domain.coverLetter.dto.internal.CoverLetterFeatureDto;
import com.cvmento.domain.coverLetter.dto.request.CoverLetterAiRequest;
import com.cvmento.domain.coverLetter.dto.response.CoverLetterAiResponse;
import com.cvmento.domain.coverLetter.dto.response.CoverLetterFeedback;
import com.cvmento.domain.coverLetter.dto.response.FeedbackItem;
import com.cvmento.domain.coverLetter.dto.response.LlmAnalysisResponse;
import com.cvmento.domain.coverLetter.entity.CoverLetterFeature;
import com.cvmento.domain.coverLetter.repository.CoverLetterFeatureRepository;
import com.cvmento.global.exception.customException.CoverLetterAiException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

    // ==============================================
    // 사용 횟수/토큰 임시 관리용 맵 (서버 재시작 시 초기화됨)
    // memberId -> 남은 토큰 수
    // 실제 운영에서는 DB 또는 Redis 사용 권장
    // private final Map<Long, Integer> usageMap = new ConcurrentHashMap<>();
    // ==============================================

    /**
     * 서버 시작 시 모든 가입 회원에게 기본 토큰 발급
     */
    // @PostConstruct
    // public void initUsageMap() {
    //     List<Member> members = memberRepository.findAll();
    //     members.forEach(member -> usageMap.put(member.getMemberId(), 10));
    // }


    /**
     * 자소서 AI 개선 메인 메서드
     */
    public CoverLetterAiResponse improveCoverLetter(CoverLetterAiRequest request, String userEmail) {
        try {
            // 0. 사용 횟수가 남아있는지 검증 (추후 구현)
            // checkUsageLimit(String userEmail);

            // 1. 특징 데이터 로드
            List<CoverLetterFeatureDto> featuresDtoList = loadCoverLetterFeatures();

            // 2. LLM 프롬프트 생성
            String prompt = llmPromptService.buildImprovementPrompt(request.content(), featuresDtoList);

            // 3. LLM API 호출
            LlmAnalysisResponse llmResponse = llmClientService.analyze(prompt);

            // 4. 피드백 파싱 및 검증
            CoverLetterFeedback feedback = parseFeedback(llmResponse.feedback());

            // 5. 최종 응답 생성
            return buildResponse(feedback, llmResponse.improvedContent());

        } catch (Exception e) {
            logError(e, request);
            throw new CoverLetterAiException("AI 개선 처리 중 오류가 발생했습니다.", e);
        }
    }

    // ==============================================
    // 토큰 체크 예시
    // ==============================================
    // private void checkUsageLimit(String String userEmail) {
    //     Integer remaining = usageMap.get(userEmail);
    //     if (remaining == null || remaining <= 0) {
    //         throw new CoverLetterAiException("AI 첨삭 사용 가능 횟수가 모두 소진되었습니다.");
    //     }
    // }

    // ==============================================
    // 주기적으로 토큰 초기화 (예: 12시간마다)
    // 실제 운영 시 @Scheduled 또는 Redis TTL 활용
    // ==============================================
    // @Scheduled(fixedRate = 12 * 60 * 60 * 1000) // 12시간
    // public void resetUsageMap() {
    //     usageMap.replaceAll((k, v) -> 10);
    // }

    /* ---------------------------- 보조 유틸리티 ---------------------------- */

    /** DB에서 우수 자소서 특징 데이터를 조회 */
    private List<CoverLetterFeatureDto> loadCoverLetterFeatures() {
        List<CoverLetterFeature> features = coverLetterFeatureRepository.findAll();
        if (features.isEmpty()) log.warn("우수 자소서 특징 데이터가 없습니다.");

        return features.stream()
                .map(f -> new CoverLetterFeatureDto(f.getFeaturesCategory().name(), f.getDescription()))
                .toList();
    }


    /** LLM API에서 반환된 피드백 JSON을 파싱 */
    private CoverLetterFeedback parseFeedback(String feedbackJson) {
        if (feedbackJson == null || feedbackJson.trim().isEmpty()) return createDefaultFeedback();

        try {
            var feedback = objectMapper.readValue(feedbackJson, CoverLetterFeedback.class);
            return validateAndCorrectFeedback(feedback);
        } catch (JsonProcessingException e) {
            log.error("피드백 JSON 파싱 실패: {}", feedbackJson, e);
            return createDefaultFeedback();
        }
    }

    /** 파싱된 피드백 객체를 검증하고 필드 보정 */
    private CoverLetterFeedback validateAndCorrectFeedback(CoverLetterFeedback feedback) {
        // 반환 타입을 명확히 FeedbackItem 리스트로 맞춘다.
        List<FeedbackItem> validStrengths = validateFeedbackItems(feedback.strengths());
        List<FeedbackItem> validImprovements = validateFeedbackItems(feedback.improvements());
        String validSummary = feedback.summary() != null ? feedback.summary() : "분석 완료";

        return new CoverLetterFeedback(validStrengths, validImprovements, validSummary);
    }

    /** 피드백 리스트에서 null 제거 및 유효 항목 필터링 (FeedbackItem 타입 고정) */
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
            improvedContent = "개선된 내용을 생성하는 중 오류가 발생했습니다.";
        }
        return new CoverLetterAiResponse(feedback, improvedContent.trim());
    }

    /** 서비스 수행 중 발생한 에러를 로깅 */
    private void logError(Exception e, CoverLetterAiRequest request) {
        log.error("자소서 AI 개선 중 오류 발생 - content length: {}, error: {}",
                request != null ? request.content().length() : 0,
                e.getMessage(), e);
    }

    private boolean isValidFeedbackItem(FeedbackItem item) {
        if (item == null) return false;

        return item.description() != null && !item.description().trim().isEmpty()
                && item.suggestion() != null && !item.suggestion().trim().isEmpty();
    }

}
