package com.cvmento.domain.interview.service;

import com.cvmento.domain.coverLetter.entity.CoverLetter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class InterviewLlmPromptService {

    public String buildQnaGenerationPrompt(CoverLetter coverLetter) {
        return buildPromptStructure() +
                buildCoverLetterSection(coverLetter) +
                buildRequestSection() +
                buildGuidelines();
    }

    private String buildPromptStructure() {
        return """
            당신은 20년 경력의 전문 면접관입니다.
            제공된 자소서를 분석하여 실제 면접에서 나올 수 있는 예상 질문과 모범 답변을 생성해주세요.
            
            """;
    }

    private String buildCoverLetterSection(CoverLetter coverLetter) {
        StringBuilder section = new StringBuilder("## 자소서 정보\n");
        section.append("**지원분야**: ").append(coverLetter.getJobField()).append("\n");
        section.append("**경력**: ").append(coverLetter.getTotalExperienceString()).append("\n");
        section.append("**제목**: ").append(coverLetter.getTitle()).append("\n\n");
        section.append("**내용**:\n").append(coverLetter.getContent()).append("\n\n");
        return section.toString();
    }

    private String buildRequestSection() {
        return """
            ## 작업 요청
            위 자소서를 바탕으로 면접에서 나올 수 있는 예상 질문 5개와 각각의 모범 답변을 생성해주세요.
            
            다음 JSON 형식으로 응답해주세요:
            
            ```json
            {
              "qnaList": [
                {
                  "question": "첫 번째 예상 질문",
                  "answer": "자소서 내용을 기반으로 한 모범 답변"
                },
                {
                  "question": "두 번째 예상 질문", 
                  "answer": "자소서 내용을 기반으로 한 모범 답변"
                }
              ]
            }
            ```
            
            """;
    }

    private String buildGuidelines() {
        return """
            ### 중요 지침
            1. **자소서 연관성**: 질문은 자소서에 언급된 구체적 경험이나 역량과 연관되어야 함
            2. **경력 수준 고려**: 신입/경력에 따라 질문 난이도 조정
            3. **분야별 특성**: 지원분야에 맞는 전문적 질문 포함
            4. **답변 품질**: 모범 답변은 구체적이고 설득력 있게 작성
            5. **한국어**: 모든 질문과 답변은 한국어로 작성
            6. **적절한 길이**: 답변은 2-3분 분량으로 작성
            """;
    }
}