package com.cvmento.domain.interview.service;

import com.cvmento.domain.coverLetter.dto.request.InputItem;
import com.cvmento.domain.coverLetter.entity.CoverLetter;
import com.cvmento.domain.coverLetter.enums.CoverLetterStatus;
import com.cvmento.domain.coverLetter.repository.CoverLetterRepository;
import com.cvmento.domain.interview.dto.response.*;
import com.cvmento.domain.interview.entity.CoverLetterQna;
import com.cvmento.domain.interview.enums.QuestionSourceType;
import com.cvmento.domain.interview.repository.CoverLetterQnaRepository;
import com.cvmento.global.common.services.MetricsService;
import com.cvmento.global.exception.customException.AiInvalidRequestException;
import com.cvmento.global.exception.customException.CoverLetterException;
import com.cvmento.global.exception.customException.InterviewException;
import com.cvmento.global.exception.customException.InterviewLimitExceededException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import org.slf4j.MDC;

/** 인터뷰 Q&A 생성·조회 서비스 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class InterviewService {

    private final CoverLetterRepository coverLetterRepository;
    private final CoverLetterQnaRepository coverLetterQnaRepository;
    private final InterviewLlmPromptService promptService;
    private final InterviewLlmClientService llmClientService;
    private final MetricsService metricsService;

    /** 기존 Q&A 조회 */
    public InterviewQnaListResponse getExistingInterviewQna(Long coverLetterId, String memberEmail) {
        MDC.put("spanId", "interview-list-service");

        try {
            CoverLetter coverLetter = findActiveCoverLetterByIdAndMember(coverLetterId, memberEmail);

            MDC.put("spanId", "interview-repository");
            List<CoverLetterQna> existingQnaList = coverLetterQnaRepository.findByCoverLetterOrderByCreatedAtAsc(coverLetter);

            MDC.put("spanId", "interview-list-service");
            log.info("면접 Q&A 조회 - 자소서 ID: {}, 기존 개수: {}", coverLetterId, existingQnaList.size());

            return buildQnaListResponse(existingQnaList);
        } catch (CoverLetterException e) {
            metricsService.incrementErrorCount("INTERVIEW_QNA_LIST_COVER_LETTER_NOT_FOUND");
            throw e;
        } catch (Exception e) {
            metricsService.incrementErrorCount("INTERVIEW_QNA_LIST_ERROR");
            throw e;
        }
    }

    /** Q&A 생성 (초기/추가 자동 판단, 최대 15개) */
    @Transactional
    public InterviewQnaListResponse createInterviewQuestions(Long coverLetterId, String memberEmail) {
        MDC.put("spanId", "interview-generation-service");

        try {
            CoverLetter coverLetter = findActiveCoverLetterByIdAndMember(coverLetterId, memberEmail);

            MDC.put("spanId", "interview-repository");
            long existingCount = coverLetterQnaRepository.countByCoverLetterAndSourceType(
                    coverLetter, QuestionSourceType.GENERATED);

            MDC.put("spanId", "interview-generation-service");
            if (existingCount >= 15) {
                log.warn("면접 질문 생성 제한 초과 - 자소서 ID: {}, 기존개수: {}", coverLetterId, existingCount);
                metricsService.incrementErrorCount("INTERVIEW_QUESTION_LIMIT_EXCEEDED");
                throw new InterviewLimitExceededException("더 이상 질문을 생성할 수 없습니다. (최대 15개)");
            }

            String type = existingCount == 0 ? "초기" : "추가";
            log.info("면접 Q&A {} 생성 시작 - 자소서 ID: {}, 기존개수: {}", type, coverLetterId, existingCount);

            List<InputItem> inputItems = buildInputItemsByCount(coverLetter, existingCount);
            InterviewQnaListResponse response = generateQuestionsWithInputItems(coverLetter, inputItems, type);

            metricsService.incrementInterviewCreatedCount();
            return response;

        } catch (CoverLetterException e) {
            metricsService.incrementErrorCount("INTERVIEW_CREATION_COVER_LETTER_NOT_FOUND");
            throw e;
        } catch (InterviewLimitExceededException e) {
            throw e;
        } catch (Exception e) {
            metricsService.incrementErrorCount("INTERVIEW_CREATION_ERROR");
            throw e;
        }
    }

    /** 커스텀 질문 답변 생성 및 저장 */
    @Transactional
    public CustomAnswerResponse createCustomAnswer(Long coverLetterId, String memberEmail, String customQuestion) {
        MDC.put("spanId", "custom-answer-service");

        try {
            CoverLetter coverLetter = findActiveCoverLetterByIdAndMember(coverLetterId, memberEmail);

            log.info("커스텀 답변 생성 시작 - 자소서 ID: {}, 질문길이: {}",
                    coverLetterId, customQuestion.length());

            MDC.put("spanId", "prompt-building-service");
            List<InputItem> inputItems = promptService.buildCustomAnswerInputItems(coverLetter, customQuestion);

            try {
                MDC.put("spanId", "custom-answer-service");
                CustomAnswerResponse response = llmClientService.generateCustomAnswer(inputItems);

                saveCustomQuestionAndAnswer(customQuestion, response, coverLetter);

                log.info("커스텀 답변 생성 완료 - 자소서 ID: {}, 답변길이: {}",
                        coverLetterId, response.answer().length());

                return response;

            } catch (AiInvalidRequestException e) {
                log.warn("부적절한 커스텀 질문 요청 - 자소서 ID: {}, 오류: {}", coverLetterId, e.getMessage());
                metricsService.incrementErrorCount("CUSTOM_ANSWER_INVALID_REQUEST");
                throw e;
            } catch (Exception e) {
                log.error("커스텀 답변 생성 실패 - 자소서 ID: {}, 오류: {}", coverLetterId, e.getMessage(), e);
                metricsService.incrementErrorCount("CUSTOM_ANSWER_GENERATION_FAILED");
                throw new InterviewException("커스텀 질문 답변 생성에 실패했습니다.", e);
            }
        } catch (CoverLetterException e) {
            metricsService.incrementErrorCount("CUSTOM_ANSWER_COVER_LETTER_NOT_FOUND");
            throw e;
        }
    }

    /** 기존 질문 수에 따라 프롬프트 선택 */
    private List<InputItem> buildInputItemsByCount(CoverLetter coverLetter, long existingCount) {
        try {
            if (existingCount == 0) {
                return promptService.buildQnaGenerationInputItems(coverLetter);
            } else {
                MDC.put("spanId", "interview-repository");
                List<String> existingQuestions = coverLetterQnaRepository.findQuestionsByCoverLetterAndSourceType(
                        coverLetter, QuestionSourceType.GENERATED);

                MDC.put("spanId", "interview-generation-service");
                return promptService.buildAdditionalQnaInputItems(coverLetter, existingQuestions);
            }
        } catch (Exception e) {
            metricsService.incrementErrorCount("INTERVIEW_PROMPT_BUILD_ERROR");
            throw e;
        }
    }

    /** 프롬프트로 LLM 호출 → 저장 → 리스트 응답 */
    private InterviewQnaListResponse generateQuestionsWithInputItems(CoverLetter coverLetter, List<InputItem> inputItems, String type) {
        try {
            InterviewLlmResponse llmResponse = llmClientService.generateQnaList(inputItems);
            List<CoverLetterQna> newQNAs = saveQnaListToDatabaseAndReturn(llmResponse.qnaList(), coverLetter);

            log.info("{} Q&A 생성 완료 - 자소서 ID: {}, 생성개수: {}",
                    type, coverLetter.getCoverLetterId(), llmResponse.qnaList().size());

            return buildNewQNAsListResponse(newQNAs);

        } catch (Exception e) {
            log.error("{} Q&A 생성 실패 - 자소서 ID: {}, 오류: {}",
                    type, coverLetter.getCoverLetterId(), e.getMessage(), e);
            metricsService.incrementErrorCount("INTERVIEW_LLM_GENERATION_FAILED");
            throw new InterviewException("질문/답변 생성에 실패했습니다.", e);
        }
    }

    /** QnA 리스트 저장 후 반환 */
    private List<CoverLetterQna> saveQnaListToDatabaseAndReturn(List<InterviewQnaDto> qnaDataList, CoverLetter coverLetter) {
        MDC.put("spanId", "interview-repository");

        try {
            List<CoverLetterQna> savedQNAs = new ArrayList<>();

            for (InterviewQnaDto qnaData : qnaDataList) {
                CoverLetterQna qna = new CoverLetterQna(qnaData.question(), coverLetter, QuestionSourceType.GENERATED);
                qna.updateAnswerAndTip(qnaData.answer(), qnaData.tip());
                CoverLetterQna savedQna = coverLetterQnaRepository.save(qna);
                savedQNAs.add(savedQna);
            }

            MDC.put("spanId", "interview-generation-service");
            log.info("Q&A DB 저장 완료 - 저장개수: {}", savedQNAs.size());

            return savedQNAs;
        } catch (Exception e) {
            metricsService.incrementErrorCount("INTERVIEW_QNA_SAVE_ERROR");
            throw e;
        }
    }

    /** QnA 리스트 → 응답 DTO */
    private InterviewQnaListResponse buildQnaListResponse(List<CoverLetterQna> qnaList) {
        List<InterviewQnaResponse> qnaResponses = qnaList.stream()
                .map(InterviewQnaResponse::from)
                .toList();

        int generatedCount = (int) qnaList.stream()
                .filter(q -> q.getSourceType() == QuestionSourceType.GENERATED)
                .count();

        return new InterviewQnaListResponse(qnaResponses, qnaList.size(), generatedCount);
    }

    /** 새로 생성된 QnA 리스트 → 응답 DTO */
    private InterviewQnaListResponse buildNewQNAsListResponse(List<CoverLetterQna> newQNAs) {
        List<InterviewQnaResponse> qnaResponses = newQNAs.stream()
                .map(InterviewQnaResponse::from)
                .toList();

        return new InterviewQnaListResponse(qnaResponses, newQNAs.size(), newQNAs.size());
    }

    /** 활성 상태 자소서 조회 */
    private CoverLetter findActiveCoverLetterByIdAndMember(Long coverLetterId, String memberEmail) {
        MDC.put("spanId", "coverletter-repository");
        CoverLetter coverLetter = coverLetterRepository.findByCoverLetterIdAndMemberEmailAndStatus(
                        coverLetterId, memberEmail, CoverLetterStatus.ACTIVE)
                .orElseThrow(() -> new CoverLetterException("자소서를 찾을 수 없습니다."));

        MDC.put("spanId", "interview-list-service");
        return coverLetter;
    }

    /** 커스텀 질문/답변 저장 */
    private void saveCustomQuestionAndAnswer(String question, CustomAnswerResponse response, CoverLetter coverLetter) {
        MDC.put("spanId", "interview-repository");

        try {
            CoverLetterQna qna = new CoverLetterQna(question, coverLetter, QuestionSourceType.CUSTOM);
            qna.updateAnswerAndTip(response.answer(), response.tip());
            coverLetterQnaRepository.save(qna);

            MDC.put("spanId", "custom-answer-service");
        } catch (Exception e) {
            metricsService.incrementErrorCount("CUSTOM_ANSWER_SAVE_ERROR");
            throw e;
        }
    }
}