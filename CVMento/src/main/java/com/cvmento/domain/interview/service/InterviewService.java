package com.cvmento.domain.interview.service;

import com.cvmento.domain.coverLetter.entity.CoverLetter;
import com.cvmento.domain.coverLetter.repository.CoverLetterRepository;
import com.cvmento.domain.interview.dto.response.InterviewLlmResponse;
import com.cvmento.domain.interview.dto.response.InterviewQnaDto;
import com.cvmento.domain.interview.dto.response.InterviewQnaListResponse;
import com.cvmento.domain.interview.dto.response.InterviewQnaResponse;
import com.cvmento.domain.interview.entity.CoverLetterQna;
import com.cvmento.domain.interview.enums.QuestionSourceType;
import com.cvmento.domain.interview.repository.CoverLetterQnaRepository;
import com.cvmento.domain.member.entity.Member;
import com.cvmento.domain.member.repository.MemberRepository;
import com.cvmento.global.exception.customException.CoverLetterException;
import com.cvmento.global.exception.customException.InterviewException;
import com.cvmento.global.exception.customException.InterviewLimitExceededException;
import com.cvmento.global.exception.customException.MemberNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class InterviewService {

    private final CoverLetterRepository coverLetterRepository;
    private final MemberRepository memberRepository;
    private final CoverLetterQnaRepository coverLetterQnaRepository;
    private final InterviewLlmPromptService promptService;
    private final InterviewLlmClientService llmClientService;

    /**
     * 기존 면접 질문/답변 조회 (순수 조회)
     */
    public InterviewQnaListResponse getExistingInterviewQna(Long coverLetterId, String userEmail) {
        CoverLetter coverLetter = findCoverLetterByIdAndUser(coverLetterId, userEmail);
        List<CoverLetterQna> existingQnaList = coverLetterQnaRepository.findByCoverLetterOrderByCreatedAtAsc(coverLetter);
        return buildQnaListResponse(existingQnaList);
    }

    /**
     * 면접 질문/답변 생성 (초기/추가 자동 판단)
     */
    @Transactional
    public InterviewQnaListResponse createInterviewQuestions(Long coverLetterId, String userEmail) {
        CoverLetter coverLetter = findCoverLetterByIdAndUser(coverLetterId, userEmail);

        long existingCount = coverLetterQnaRepository.countByCoverLetterAndSourceType(
                coverLetter, QuestionSourceType.GENERATED);

        if (existingCount >= 15) {
            throw new InterviewLimitExceededException("더 이상 질문을 생성할 수 없습니다. (최대 15개)");
        }

        String prompt = buildPromptByCount(coverLetter, existingCount);
        return generateQuestionsWithPrompt(coverLetter, prompt, existingCount == 0 ? "초기" : "추가");
    }

    // ======================== 유틸리티 메서드 ========================

    private String buildPromptByCount(CoverLetter coverLetter, long existingCount) {
        if (existingCount == 0) {
            return promptService.buildQnaGenerationPrompt(coverLetter);
        } else {
            List<String> existingQuestions = coverLetterQnaRepository.findQuestionsByCoverLetterAndSourceType(
                    coverLetter, QuestionSourceType.GENERATED);
            return promptService.buildAdditionalQnaPrompt(coverLetter, existingQuestions);
        }
    }

    private InterviewQnaListResponse generateQuestionsWithPrompt(CoverLetter coverLetter, String prompt, String type) {
        try {
            InterviewLlmResponse llmResponse = llmClientService.generateQnaList(prompt);
            List<CoverLetterQna> newQnas = saveQnaListToDatabaseAndReturn(llmResponse.qnaList(), coverLetter);

            log.info("{} 질문/답변 {}개 생성 완료 - 자소서 ID: {}",
                    type, llmResponse.qnaList().size(), coverLetter.getCoverLetterId());

            return buildNewQnaListResponse(newQnas);

        } catch (Exception e) {
            log.error("{} 질문/답변 생성 실패 - 자소서 ID: {}", type, coverLetter.getCoverLetterId(), e);
            throw new InterviewException("질문/답변 생성에 실패했습니다.", e);
        }
    }

    private List<CoverLetterQna> saveQnaListToDatabaseAndReturn(List<InterviewQnaDto> qnaDataList, CoverLetter coverLetter) {
        List<CoverLetterQna> savedQnas = new ArrayList<>();

        for (InterviewQnaDto qnaData : qnaDataList) {
            CoverLetterQna qna = new CoverLetterQna(qnaData.question(), coverLetter);
            qna.updateAnswerAndTip(qnaData.answer(), qnaData.tip());
            CoverLetterQna savedQna = coverLetterQnaRepository.save(qna);
            savedQnas.add(savedQna);
        }

        return savedQnas;
    }

    private InterviewQnaListResponse buildQnaListResponse(List<CoverLetterQna> qnaList) {
        List<InterviewQnaResponse> qnaResponses = qnaList.stream()
                .map(InterviewQnaResponse::from)
                .toList();

        int generatedCount = (int) qnaList.stream()
                .filter(q -> q.getSourceType() == QuestionSourceType.GENERATED)
                .count();

        return new InterviewQnaListResponse(qnaResponses, qnaList.size(), generatedCount);
    }

    private InterviewQnaListResponse buildNewQnaListResponse(List<CoverLetterQna> newQnas) {
        List<InterviewQnaResponse> qnaResponses = newQnas.stream()
                .map(InterviewQnaResponse::from)
                .toList();

        return new InterviewQnaListResponse(qnaResponses, newQnas.size(), newQnas.size());
    }

    private CoverLetter findCoverLetterByIdAndUser(Long coverLetterId, String userEmail) {
        Member member = memberRepository.findByEmail(userEmail)
                .orElseThrow(() -> new MemberNotFoundException("사용자를 찾을 수 없습니다."));

        return coverLetterRepository.findByCoverLetterIdAndMember(coverLetterId, member)
                .orElseThrow(() -> new CoverLetterException("자소서를 찾을 수 없습니다."));
    }
}