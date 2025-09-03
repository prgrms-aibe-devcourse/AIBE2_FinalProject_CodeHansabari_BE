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
import com.cvmento.global.exception.customException.MemberNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
     * 자소서 기반 예상 질문/답변 생성 또는 기존 데이터 조회
     */
    @Transactional
    public InterviewQnaListResponse generateOrGetInterviewQna(Long coverLetterId, String userEmail) {
        CoverLetter coverLetter = findCoverLetterByIdAndUser(coverLetterId, userEmail);

        List<CoverLetterQna> existingQnaList = coverLetterQnaRepository.findByCoverLetterOrderByCreatedAtAsc(coverLetter);

        if (existingQnaList.isEmpty()) {
            // 첫 질문/답변 생성
            generateAndSaveQnaList(coverLetter);
            existingQnaList = coverLetterQnaRepository.findByCoverLetterOrderByCreatedAtAsc(coverLetter);
        }

        return buildQnaListResponse(existingQnaList);
    }

    // ======================== 유틸리티 메서드 ========================

    private void generateAndSaveQnaList(CoverLetter coverLetter) {
        try {
            String prompt = promptService.buildQnaGenerationPrompt(coverLetter);
            InterviewLlmResponse llmResponse = llmClientService.generateQnaList(prompt);

            saveQnaListToDatabase(llmResponse.qnaList(), coverLetter);

            log.info("질문/답변 {}개 생성 완료 - 자소서 ID: {}",
                    llmResponse.qnaList().size(), coverLetter.getCoverLetterId());

        } catch (Exception e) {
            log.error("질문/답변 생성 및 저장 실패 - 자소서 ID: {}", coverLetter.getCoverLetterId(), e);
            throw new InterviewException("질문/답변 생성 및 저장에 실패했습니다.", e);
        }
    }

    private void saveQnaListToDatabase(List<InterviewQnaDto> qnaDataList, CoverLetter coverLetter) {
        for (InterviewQnaDto qnaData : qnaDataList) {
            CoverLetterQna qna = new CoverLetterQna(qnaData.question(), coverLetter);
            qna.updateAnswer(qnaData.answer());
            coverLetterQnaRepository.save(qna);
        }
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

    private CoverLetter findCoverLetterByIdAndUser(Long coverLetterId, String userEmail) {
        Member member = memberRepository.findByEmail(userEmail)
                .orElseThrow(() -> new MemberNotFoundException("사용자를 찾을 수 없습니다."));

        return coverLetterRepository.findByCoverLetterIdAndMember(coverLetterId, member)
                .orElseThrow(() -> new CoverLetterException("자소서를 찾을 수 없습니다."));
    }
}