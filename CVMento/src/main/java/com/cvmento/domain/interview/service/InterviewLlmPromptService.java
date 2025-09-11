package com.cvmento.domain.interview.service;

import com.cvmento.domain.coverLetter.entity.CoverLetter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import org.slf4j.MDC;

@Slf4j
@Service
@RequiredArgsConstructor
public class InterviewLlmPromptService {

    // ======================== Public Methods ========================

    public String buildQnaGenerationPrompt(CoverLetter coverLetter) {
        MDC.put("spanId", "prompt-building-service");

        log.info("초기 Q&A 프롬프트 생성 - 자소서ID: {}, 지원분야: {}",
                coverLetter.getCoverLetterId(), coverLetter.getJobField());

        String prompt = buildPromptStructure() +
                buildCoverLetterSection(coverLetter) +
                buildInitialRequestSection() +
                buildCompleteGuidelines();

        log.info("초기 Q&A 프롬프트 생성 완료 - 총 길이: {}chars", prompt.length());

        return prompt;
    }

    public String buildAdditionalQnaPrompt(CoverLetter coverLetter, List<String> existingQuestions) {
        MDC.put("spanId", "prompt-building-service");

        log.info("추가 Q&A 프롬프트 생성 - 자소서ID: {}, 기존질문수: {}",
                coverLetter.getCoverLetterId(), existingQuestions.size());

        String prompt = buildPromptStructure() +
                buildCoverLetterSection(coverLetter) +
                buildExistingQuestionsSection(existingQuestions) +
                buildAdditionalRequestSection() +
                buildAdditionalCompleteGuidelines();

        log.info("추가 Q&A 프롬프트 생성 완료 - 총 길이: {}chars", prompt.length());

        return prompt;
    }

    public String buildCustomAnswerPrompt(CoverLetter coverLetter, String customQuestion) {
        MDC.put("spanId", "prompt-building-service");

        log.info("커스텀 답변 프롬프트 생성 - 자소서ID: {}, 질문길이: {}",
                coverLetter.getCoverLetterId(), customQuestion.length());

        String prompt = buildCustomAnswerPromptStructure() +
                buildCoverLetterSection(coverLetter) +
                buildCustomQuestionSection(customQuestion) +
                buildCustomAnswerRequestSection() +
                buildAnswerOnlyGuidelines();

        log.info("커스텀 답변 프롬프트 생성 완료 - 총 길이: {}chars", prompt.length());

        return prompt;
    }

    // ======================== Common Sections ========================

    private String buildPromptStructure() {
        return """
            당신은 20년 경력의 전문 면접관입니다.
            제공된 자소서를 분석하여 실제 면접에서 나올 수 있는 예상 질문과 모범 답변을 생성해주세요.
            
            """;
    }

    private String buildCustomAnswerPromptStructure() {
        return """
        당신은 면접을 준비하는 구직자입니다.
        제공된 자소서를 바탕으로 면접관의 질문에 대해 효과적이고 설득력 있는 답변을 준비해주세요.
        
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

    private String buildExistingQuestionsSection(List<String> existingQuestions) {
        StringBuilder section = new StringBuilder("## 기존 생성된 질문들\n");
        section.append("다음 질문들은 이미 생성되었으므로 **절대 중복되지 않도록** 해주세요:\n\n");

        for (int i = 0; i < existingQuestions.size(); i++) {
            section.append(String.format("%d. %s\n", i + 1, existingQuestions.get(i)));
        }
        section.append("\n");
        return section.toString();
    }

    private String buildCustomQuestionSection(String customQuestion) {
        return """
        ## 면접관의 질문
        다음 질문에 대해 위 자소서를 기반으로 한 모범 답변을 준비해주세요:
        
        **질문**: """ + customQuestion + "\n\n";
    }

    // ======================== Request Sections ========================

    private String buildInitialRequestSection() {
        return """
            ## 작업 요청
            위 자소서를 바탕으로 면접에서 나올 수 있는 예상 질문 5개와 각각의 모범 답변, 그리고 실용적인 면접 팁을 생성해주세요.
            
            """ + buildQnaListJsonFormat();
    }

    private String buildAdditionalRequestSection() {
        return """
            ## 작업 요청
            기존 질문들과 **완전히 다른 관점**에서 새로운 예상 질문 5개를 생성해주세요.
            
            **중복 방지 원칙:**
            - 기존 질문과 유사한 의도나 답변을 요구하는 질문 금지
            - 다른 각도에서 지원자의 역량을 평가할 수 있는 질문 생성
            - 새로운 상황이나 시나리오 기반 질문 포함
            
            """ + buildQnaListJsonFormat();
    }

    private String buildCustomAnswerRequestSection() {
        return """
        ## 답변 작성 요청
        위 면접관의 질문에 대해 자소서 내용을 기반으로 한 효과적인 답변과 답변 시 주의사항을 작성해주세요.
        
        """ + buildSingleAnswerJsonFormat();
    }

    // ======================== JSON Format Sections ========================

    private String buildQnaListJsonFormat() {
        return """
        다음 JSON 형식으로 응답해주세요:
        
        ```json
        {
          "qnaList": [
            {
              "question": "첫 번째 예상 질문",
              "answer": "자소서 내용을 기반으로 한 모범 답변",
              "tip": "실용적인 답변 팁"
            },
            {
              "question": "두 번째 예상 질문",
              "answer": "자소서 내용을 기반으로 한 모범 답변",
              "tip": "실용적인 답변 팁"
            },
            {
              "question": "세 번째 예상 질문",
              "answer": "자소서 내용을 기반으로 한 모범 답변",
              "tip": "실용적인 답변 팁"
            }
            // ... 총 5개의 질문과 답변
          ]
        }
        ```
        
        """;
    }

    private String buildSingleAnswerJsonFormat() {
        return """
            다음 JSON 형식으로 응답해주세요:
            
            ```json
            {
              "answer": "자소서 내용을 기반으로 한 구체적이고 설득력 있는 모범 답변",
              "tip": "이 질문에 대한 실용적인 답변 팁과 주의사항"
            }
            ```
            
            """;
    }

    // ======================== Guideline Components ========================

    private String buildQuestionGenerationGuidelines() {
        return """
        **질문 생성:**
        1. **자소서 연관성**: 질문은 자소서에 언급된 구체적 경험이나 역량과 연관되어야 함
        2. **경력 수준 고려**: 
           - 신입(0-2년): 학습 과정, 기본 역량, 성장 의지 중심
           - 경력(3년+): 전문성, 성과, 리더십, 문제해결 경험 중심
        3. **분야별 특성**: 지원분야에 맞는 전문적 질문 포함
        4. **질문 유형 다양성**: 경험, 역량, 기술, 동기, 상황형 질문 포함
        5. **적절한 난이도**: 압박보다는 경험 공유와 역량 확인 위주의 현실적인 질문
        
        """;
    }

    private String buildAnswerGenerationGuidelines() {
        return """
        **답변 생성:**
        1. **적절한 길이**: 각 답변은 250~350자로 작성 (실제 면접에서 1~2분 구술 분량)
        2. **자연스러운 대화체**: 
           - 실제 면접에서 말하는 것처럼 자연스럽게 작성
           - "정의:", "이유:", "예시:" 등의 구조 라벨 절대 사용 금지
           - 한 문단으로 자연스럽게 연결되는 서술형 답변
        3. **질문 유형별 답변 패턴**: 
           - 기술 질문: 기술 설명 위주, 자소서 연결 금지 (미래 계획/포부 금지)
           - 경험 질문: 구체적 상황 + 행동 + 결과
           - 가치관 질문: 개인 가치관 + 자소서 경험 연결
        4. **논리적 흐름**: 
           - 핵심 메시지 → 근거/설명 → 구체적 경험 순으로 자연스럽게 연결
           - 문장 간 자연스러운 연결어 사용 ("그래서", "때문에", "또한" 등)
        5. **표현 방식**: 
           - "구체적으로", "체계적으로" 등 수식어 남용 금지
           - "~하겠습니다" 반복 사용 지양, 다양한 마무리 표현 활용
           - 모호한 표현 대신 구체적 사례와 기술명 활용
        6. **현실적 수준**: 신입 개발자 수준에 맞는 경험과 성과만 언급
        7. **톤**: 겸손하되 자신감 있게, 과도한 포부나 다짐 지양
        
        """;
    }

    private String buildTipGenerationGuidelines() {
        return """
        **팁 생성:**
        1. **실용성**: 면접 현장에서 즉시 적용 가능한 구체적 조언
        2. **길이**: 60~100자의 간결한 한 문장
        3. **톤**: 친근한 조언 형태, 지시문 느낌 배제
        4. **내용 구성**:\s
           - 답변 시 강조할 포인트나 주의사항
           - 자소서와의 자연스러운 연결 방법 제시 (해당되는 경우에만)
           - 면접관이 추가로 궁금해할 수 있는 부분 예상
        5. **자소서 연결 가이드**:\s
           - 기술 질문의 경우, 자소서의 관련 프로젝트나 경험과 연결하는 방법 제시
           - "○○ 프로젝트 경험과 연결해서 설명하면 더 설득력 있어요" 형태
        
        """;
    }

    private String buildCommonRequirements() {
        return """
        **공통 요구사항:**
        - 한국어 작성, 자소서와 일관성 유지
        - 신입 수준에 맞는 현실적 경험과 목표 설정
        - 과장/허구 절대 금지, 겸손하면서도 적극적인 태도
        - 반복 표현 최소화, 자연스러운 어조 유지
        - 개인정보 생성 금지
        """;
    }

    private String buildAdditionalQuestionGuidelines() {
        return """
        
        **추가 질문 생성 시 고려사항:**
        - **새로운 질문 유형**: 상황 판단, 갈등 해결, 미래 비전, 윤리적 딜레마 등
        - **다른 관점**: 팀워크, 리더십, 학습능력, 스트레스 관리, 의사소통 등
        - **심화 질문**: 기존 답변을 더 깊이 파고들 수 있는 후속 질문
        - **실무 상황**: 구체적인 업무 시나리오나 문제 해결 상황
        - **중복 방지**: 기존 질문의 핵심 키워드나 의도와 겹치지 않도록 주의
        """;
    }

    private String buildAnswerAdditionalCautions() {
        return """
        #### 추가 주의사항
        - 자소서 우선: 질문과 직접 연결되는 자소서 경험/가치관을 최우선으로 사용하라.
        - 없다 → 유사 경험 연결(가정 명시): 직접 사례가 없을 경우, 가장 인접한 경험으로 추론하되
          다음과 같은 전환 문구를 사용하여 가정임을 명확히 하라.
          · "자소서에서 언급한 ○○ 경험을 바탕으로 보면…"
          · "직접 사례는 없지만, 유사 상황에서는 이렇게 대응했을 것 같다…"
        - 상식/일반 질문: 자소서와 연결 가능하면 간단히 연결하고, 어려우면 지원자로서의
          기본 태도/가치관을 간결히 제시한 뒤 자소서 키워드를 가볍게 보강하라.
          · "자소서에 직접 언급하진 않았지만, 내가 중요하게 생각하는 것은 △△이며,
             □□ 경험에서도 이 점을 중시했다."
        - 금지/주의: 자소서와 모순되는 사실 생성 금지, 과도한 추측 및 개인정보 생성 금지.
          불확실성은 전환 문구를 사용해 범위를 한정하라.
        """;
    }

    // ======================== Complete Guidelines ========================

    private String buildCompleteGuidelines() {
        return "### 중요 지침\n" +
                buildQuestionGenerationGuidelines() +
                buildAnswerGenerationGuidelines() +
                buildTipGenerationGuidelines() +
                buildCommonRequirements();
    }

    private String buildAdditionalCompleteGuidelines() {
        return "### 중요 지침\n" +
                buildQuestionGenerationGuidelines() +
                buildAnswerGenerationGuidelines() +
                buildTipGenerationGuidelines() +
                buildCommonRequirements() +
                buildAdditionalQuestionGuidelines();
    }

    private String buildAnswerOnlyGuidelines() {
        return "### 중요 지침\n" +
                buildAnswerGenerationGuidelines() +
                buildTipGenerationGuidelines() +
                buildCommonRequirements()+
                buildAnswerAdditionalCautions();
    }
}