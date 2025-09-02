package com.cvmento.domain.coverLetter.seed;

import com.cvmento.domain.coverLetter.entity.CoverLetterFeature;
import com.cvmento.domain.coverLetter.enums.FeaturesCategory;
import com.cvmento.domain.coverLetter.repository.CoverLetterFeatureRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CoverLetterFeatureSeeder implements CommandLineRunner {

    private final CoverLetterFeatureRepository repository;

    @Override
    public void run(String... args) throws Exception {
        if (repository.count() > 0) {
            System.out.println("이미 CoverLetterFeature 데이터가 존재합니다.");
            return;
        }

        // STRUCTURE 34개
        for (int i = 1; i <= 34; i++) {
            String desc = switch (i) {
                case 1 -> "자소서 서두에서 지원 동기와 핵심 강점을 명확하게 제시함";
                case 2 -> "경험 중심의 본문이 시간 순서가 아닌 중요도 순으로 구성됨";
                case 3 -> "각 항목별로 소제목을 활용하여 가독성 높음";
                case 4 -> "문단마다 핵심 메시지가 첫 문장에 명확히 드러남";
                case 5 -> "도입, 본문, 결론 구조가 논리적 흐름을 유지함";
                case 6 -> "성과와 경험을 나누어 단계적으로 설명함";
                case 7 -> "핵심 경험을 중심으로 3단 구조를 활용함";
                case 8 -> "첫 문단에서 자신을 한 줄로 요약하는 표현 포함";
                case 9 -> "중간 문단에서 문제-행동-성과(STAR) 구조 활용";
                case 10 -> "각 경험에 대한 배경을 간결하게 서술함";
                case 11 -> "결론에서 지원 직무와 연결되는 요약 제공";
                case 12 -> "문단 길이가 3~5문장으로 일정하게 구성됨";
                case 13 -> "중복 경험 없이 다양한 사례를 균형 있게 제시함";
                case 14 -> "숫자와 통계를 활용해 성과를 구체화함";
                case 15 -> "지원 직무와 관련 없는 경험을 최소화함";
                case 16 -> "논리적 흐름을 위해 접속사를 적절히 사용함";
                case 17 -> "문단 간 연결 문구를 통해 자연스러운 흐름 유지";
                case 18 -> "강점과 개선 경험을 명확히 분리함";
                case 19 -> "자기소개서 길이가 적절하게 분배됨";
                case 20 -> "중요 내용은 문장 시작에 배치함";
                case 21 -> "각 경험별 결과를 명확히 언급함";
                case 22 -> "핵심 메시지를 반복하며 강조함";
                case 23 -> "문장 간 논리적 연결성을 강조함";
                case 24 -> "경험의 배경과 맥락을 충분히 제공함";
                case 25 -> "결과와 배운 점을 한 문장으로 요약함";
                case 26 -> "문단별 중심 문장이 명확함";
                case 27 -> "중요 성과를 강조하기 위해 문단 앞부분에 위치";
                case 28 -> "지원 직무와 직접 연관된 경험만 포함";
                case 29 -> "각 문단마다 핵심 키워드를 포함";
                case 30 -> "도입부에서 전체 구조를 간략히 안내함";
                case 31 -> "논리적 순서로 경험과 성과를 배열함";
                case 32 -> "STAR 구조를 대부분 경험에 적용함";
                case 33 -> "마무리 문단에서 강점과 지원 동기 재강조";
                case 34 -> "자기소개서 전체 길이가 2~3문단으로 균형적";
                default -> "자소서 구조 관련 특징 #" + i;
            };
            repository.save(new CoverLetterFeature(FeaturesCategory.STRUCTURE, desc));
        }

        // CONTENT 33개
        for (int i = 1; i <= 33; i++) {
            String desc = switch (i) {
                case 1 -> "팀 프로젝트에서 맡은 역할과 구체적 성과를 명시함";
                case 2 -> "문제 해결 과정과 행동을 구체적으로 기술함";
                case 3 -> "성과 지표나 수치로 결과를 구체화함";
                case 4 -> "업무 관련 핵심 경험 위주로 구성함";
                case 5 -> "지원 직무와 관련 있는 프로젝트 중심으로 작성";
                case 6 -> "문제 상황과 해결 방법을 단계적으로 기술함";
                case 7 -> "성과 중심으로 경험을 압축하여 표현함";
                case 8 -> "팀 내 기여도를 명확하게 기술함";
                case 9 -> "실제 사례 기반으로 구체적인 경험 서술";
                case 10 -> "책임과 역할을 분명히 구분하여 표현";
                case 11 -> "직무 역량과 연결되는 경험 강조";
                case 12 -> "성공/실패 경험 모두에서 배운 점 명시";
                case 13 -> "협업 경험과 팀 기여를 구체적으로 표현";
                case 14 -> "문제 해결 능력을 보여주는 경험 포함";
                case 15 -> "업무 성과와 직무 역량을 연계함";
                case 16 -> "프로젝트 목표와 결과를 명확히 연결";
                case 17 -> "전문 기술을 활용한 사례 구체화";
                case 18 -> "성과를 달성한 과정과 수치를 상세히 작성";
                case 19 -> "자발적 개선 및 혁신 사례 포함";
                case 20 -> "업무 효율화 경험과 구체적 방법 기술";
                case 21 -> "직무 관련 자격이나 기술 경험 명시";
                case 22 -> "문제 해결 과정의 단계별 행동 설명";
                case 23 -> "성과 중심으로 경험을 압축하여 표현";
                case 24 -> "팀워크와 소통 능력을 보여주는 사례";
                case 25 -> "지원 직무 핵심 역량과 연계된 경험";
                case 26 -> "자발적 프로젝트 참여 경험 명시";
                case 27 -> "성과 달성 과정에서의 핵심 행동 강조";
                case 28 -> "문제 상황에서 주도적 역할 수행 사례";
                case 29 -> "업무 난이도와 해결 방식을 구체적 설명";
                case 30 -> "기술/역량 적용 사례를 명확히 제시";
                case 31 -> "업무 과정에서 개선 사항을 반영한 경험";
                case 32 -> "지원 직무 관련 문제 해결 경험 강조";
                case 33 -> "프로젝트 성과와 팀 기여도를 수치화";
                default -> "자소서 내용 관련 특징 #" + i;
            };
            repository.save(new CoverLetterFeature(FeaturesCategory.CONTENT, desc));
        }

        // EXPRESSION 33개
        for (int i = 1; i <= 33; i++) {
            String desc = switch (i) {
                case 1 -> "문장이 간결하고 핵심 메시지가 명확함";
                case 2 -> "적절한 연결어와 접속사를 사용하여 문장 흐름 자연스러움";
                case 3 -> "능동형 표현을 사용하여 적극적 인상 전달";
                case 4 -> "직무 관련 키워드를 자연스럽게 포함함";
                case 5 -> "중복 단어를 최소화하고 다양하게 표현";
                case 6 -> "적절한 강조어 사용으로 주목도 높임";
                case 7 -> "문장 길이가 다양하지만 전체적으로 읽기 편함";
                case 8 -> "전문 용어를 과도하지 않게 적절히 사용";
                case 9 -> "경험과 성과를 구체적 표현으로 강조";
                case 10 -> "문장 끝맺음이 명확하여 이해 용이";
                case 11 -> "강조할 부분을 문장 위치로 배치";
                case 12 -> "읽는 사람 관점에서 자연스러운 문장 구성";
                case 13 -> "부정적 표현보다 긍정적 표현 사용";
                case 14 -> "문장에 숫자나 지표를 자연스럽게 포함";
                case 15 -> "주어와 동사를 명확히 하여 의미 분명";
                case 16 -> "한 문장 안에서 메시지를 명확히 전달";
                case 17 -> "적절한 단락 구분으로 가독성 향상";
                case 18 -> "행동 중심 동사 사용으로 역동적 표현";
                case 19 -> "중복되는 표현을 피하고 다양한 표현 사용";
                case 20 -> "문장마다 핵심 키워드 포함";
                case 21 -> "표현이 자연스럽고 읽기 흐름이 부드러움";
                case 22 -> "경험과 성과 강조를 위해 적절한 수식어 사용";
                case 23 -> "결과 중심 서술을 위해 문장 구조 조정";
                case 24 -> "문장 앞부분에 핵심 메시지 배치";
                case 25 -> "읽는 사람이 쉽게 이해할 수 있는 표현 사용";
                case 26 -> "과도한 수식 없이 간결한 표현 유지";
                case 27 -> "자신의 강점을 부드럽게 강조";
                case 28 -> "행동과 결과를 구체적 문장으로 표현";
                case 29 -> "적절한 비유나 예시로 이해 도움";
                case 30 -> "문장 톤이 일관되고 전문적임";
                case 31 -> "표현이 명확하여 평가자가 이해하기 쉬움";
                case 32 -> "중요 메시지를 반복하여 강조";
                case 33 -> "자연스러운 문장 흐름으로 전체 읽기 편함";
                default -> "자소서 표현 관련 특징 #" + i;
            };
            repository.save(new CoverLetterFeature(FeaturesCategory.EXPRESSION, desc));
        }

        System.out.println("CoverLetterFeature 100개 시딩 완료!");
    }
}
