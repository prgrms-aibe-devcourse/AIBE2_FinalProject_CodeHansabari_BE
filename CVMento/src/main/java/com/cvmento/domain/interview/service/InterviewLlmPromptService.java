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
            위 자소서를 바탕으로 면접에서 나올 수 있는 예상 질문 5개와 각각의 모범 답변, 그리고 실용적인 면접 팁을 생성해주세요.
            
            다음 JSON 형식으로 응답해주세요:
            
            ```json
            {
              "qnaList": [
                {
                  "question": "첫 번째 예상 질문",
                  "answer": "자소서 내용을 기반으로 한 모범 답변",
                  "tip": "이 질문에 대한 실용적인 답변 팁과 주의사항"
                },
                {
                  "question": "두 번째 예상 질문", 
                  "answer": "자소서 내용을 기반으로 한 모범 답변",
                  "tip": "이 질문에 대한 실용적인 답변 팁과 주의사항"
                }
              ]
            }
            ```
            
            """;
    }

    private String buildGuidelines() {
        return """
        ### 중요 지침
        **질문 생성:**
        1. **자소서 연관성**: 질문은 자소서에 언급된 구체적 경험이나 역량과 연관되어야 함
        2. **경력 수준 고려**: 신입/경력에 따라 질문 난이도 조정
        3. **분야별 특성**: 지원분야에 맞는 전문적 질문 포함
        4. **질문 유형 다양성**: 경험, 역량, 기술, 동기, 상황형 질문 포함
        
        **답변 생성:**
        1. **구체성**: 자소서의 경험을 바탕으로 한 구체적이고 설득력 있는 답변 (만일, 자소서 경험과 관련 없는 기술적인 질문일 경우, 일반적인 답변 작성)
        2. **적절한 길이**: 각 답변은 350~500자 내외로 작성 (실제 면접에서 2~3분 구술 분량)
        3. **답변 구조**: PREP(Point-Reason-Example-Point)을 기본으로 사용, 기술 질문의 경우 DREW(Definition-Reason/Principle-Example-What to watch)로 작성
        4. **톤**: 명확·간결·자신감. 책임 표현 사용, 모호한 표현 최소화
        
        **팁 생성:**
        1. **실용성**: 실제 면접에서 바로 활용할 수 있는 구체적인 조언
        2. **주의사항**: 피해야 할 실수나 함정에 대한 경고
        3. **개선점**: 답변을 더욱 효과적으로 만들 수 있는 방법
        4. **적절한 길이**: 각 항목에 tip은 정확히 1개, 한 문장으로 제공하되 최소 60자 이상 120자 이하로 작성
        5. **설명형 톤**: 지시가 아니라, 조언·해설처럼 자연스럽게 작성
        
        **공통 요구사항:**
        - 모든 내용은 한국어로 작성
        - 자소서 맥락과 일관성 유지, 과장/허구 금지
        - 면접관의 입장에서 평가하고 싶은 요소 반영
        - 신입은 학습 의지/성장 잠재력 강조, 경력자는 성과/지표 강조
        - 개인정보/민감정보 생성 금지
        """;
    }
}