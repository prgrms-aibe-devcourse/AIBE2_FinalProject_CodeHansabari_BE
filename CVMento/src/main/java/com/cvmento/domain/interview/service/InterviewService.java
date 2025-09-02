package com.cvmento.domain.interview.service;

import com.cvmento.domain.coverLetter.entity.CoverLetter;
import com.cvmento.domain.coverLetter.repository.CoverLetterRepository;
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
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
    private final ObjectMapper objectMapper;

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
            String qnaListJson = llmClientService.generateQnaList(prompt);

            List<QnaData> qnaDataList = parseQnaListFromJson(qnaListJson);
            saveQnaListToDatabase(qnaDataList, coverLetter);

            log.info("질문/답변 {}개 생성 완료 - 자소서 ID: {}",
                    qnaDataList.size(), coverLetter.getCoverLetterId());

        } catch (Exception e) {
            log.error("질문/답변 생성 및 저장 실패", e);
            throw new InterviewException("질문/답변 생성 및 저장에 실패했습니다.", e);
        }
    }

    private List<QnaData> parseQnaListFromJson(String qnaListJson) {
        try {
            List<QnaData> qnaDataList = new ArrayList<>();

            if (qnaListJson.trim().startsWith("{")) {
                JsonNode jsonNode = objectMapper.readTree(qnaListJson);

                if (jsonNode.has("qnaList")) {
                    JsonNode qnaArray = jsonNode.get("qnaList");
                    if (qnaArray.isArray()) {
                        for (JsonNode qnaNode : qnaArray) {
                            if (qnaNode.has("question") && qnaNode.has("answer")) {
                                String question = qnaNode.get("question").asText();
                                String answer = qnaNode.get("answer").asText();
                                qnaDataList.add(new QnaData(question, answer));
                            }
                        }
                    }
                }
            }

            if (qnaDataList.isEmpty()) {
                // JSON 파싱 실패 시 기본 질문/답변 제공
                qnaDataList.addAll(getDefaultQnaList());
            }

            return qnaDataList;

        } catch (Exception e) {
            log.error("질문/답변 JSON 파싱 실패: {}", qnaListJson, e);
            return getDefaultQnaList();
        }
    }

    private List<QnaData> getDefaultQnaList() {
        return List.of(
                new QnaData("자기소개를 해주세요.", "안녕하세요. 저는 열정적이고 성장지향적인 개발자입니다."),
                new QnaData("이 분야를 선택한 이유는 무엇인가요?", "기술을 통해 문제를 해결하는 것에 큰 보람을 느끼기 때문입니다."),
                new QnaData("가장 도전적이었던 프로젝트에 대해 설명해주세요.", "팀 프로젝트에서 기술적 난제를 해결한 경험이 있습니다."),
                new QnaData("팀워크 경험에 대해 말씀해주세요.", "다양한 팀 프로젝트에서 협업과 소통의 중요성을 배웠습니다."),
                new QnaData("향후 5년간의 계획은 무엇인가요?", "지속적인 학습을 통해 전문성을 키우고 싶습니다.")
        );
    }

    private void saveQnaListToDatabase(List<QnaData> qnaDataList, CoverLetter coverLetter) {
        for (QnaData qnaData : qnaDataList) {
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

    // 내부 데이터 클래스
    private record QnaData(String question, String answer) {}
}