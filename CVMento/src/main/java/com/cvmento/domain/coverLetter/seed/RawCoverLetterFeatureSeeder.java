package com.cvmento.domain.coverLetter.seed;

import com.cvmento.domain.coverLetter.entity.RawCoverLetterFeature;
import com.cvmento.domain.coverLetter.enums.FeaturesCategory;
import com.cvmento.domain.coverLetter.repository.RawCoverLetterFeatureRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Raw Cover Letter Feature 데이터 시더 (테스트용)
 * - 하드코딩으로 314개 자소서 ID 기반 RawCoverLetterFeature 생성
 * - 각 카테고리별로 314개씩 특징 생성 (중복 허용)
 * - EXPRESSION: 314개, STRUCTURE: 314개, CONTENT: 314개 = 총 942개 특징 생성
 * - 중복 제거 테스트용으로 동일한 특징이 여러 번 생성됨
 * - 테스트 완료 후 삭제 예정
 */
@Component  // 활성화: 942개 하드코딩 특징으로 다양성 확보
@RequiredArgsConstructor
@Slf4j
public class RawCoverLetterFeatureSeeder {

    private final RawCoverLetterFeatureRepository rawCoverLetterFeatureRepository;

    //@EventListener(ApplicationReadyEvent.class) 필요할 때 사용
    public void seedRawCoverLetterFeatures() {
        if (rawCoverLetterFeatureRepository.count() > 0) {
            log.info("RawCoverLetterFeature 데이터가 이미 존재합니다. 시딩을 건너뜁니다.");
            return;
        }

        log.info("RawCoverLetterFeature 데이터 시딩 시작 - 하드코딩 314개 자소서 처리");
        
        List<RawCoverLetterFeature> rawFeatures = new ArrayList<>();
        
        // 각 카테고리별로 314개씩 특징 생성 (하드코딩, 중복 허용)
        for (int i = 1; i <= 314; i++) {
            rawFeatures.add(createFeature((long) i, FeaturesCategory.EXPRESSION));
        }
        
        for (int i = 1; i <= 314; i++) {
            rawFeatures.add(createFeature((long) i, FeaturesCategory.STRUCTURE));
        }
        
        for (int i = 1; i <= 314; i++) {
            rawFeatures.add(createFeature((long) i, FeaturesCategory.CONTENT));
        }
        
        rawCoverLetterFeatureRepository.saveAll(rawFeatures);
        log.info("RawCoverLetterFeature 데이터 시딩 완료 - 총 {}개 특징 생성", rawFeatures.size());
    }

    /**
     * 카테고리별 특징 생성
     */
    private RawCoverLetterFeature createFeature(Long coverLetterId, FeaturesCategory category) {
        String description = generateFeatureDescription(category, coverLetterId);
        return new RawCoverLetterFeature(category, description, coverLetterId);
    }

    /**
     * 카테고리별 특징 설명 생성 (각 자소서마다 다른 특징)
     */
    private String generateFeatureDescription(FeaturesCategory category, Long coverLetterId) {
        // 각 자소서마다 다른 특징을 생성하기 위해 coverLetterId를 직접 사용
        return switch (category) {
            case EXPRESSION -> getExpressionFeature((int) (coverLetterId % 33));
            case STRUCTURE -> getStructureFeature((int) (coverLetterId % 34));
            case CONTENT -> getContentFeature((int) (coverLetterId % 33));
        };
    }

    /**
     * EXPRESSION 카테고리 특징들 (순서대로 선택)
     */
    private String getExpressionFeature(int index) {
        String[] features = {
            "문장이 간결하고 핵심 메시지가 명확함",
            "적절한 연결어와 접속사를 사용하여 문장 흐름 자연스러움",
            "능동형 표현을 사용하여 적극적 인상 전달",
            "직무 관련 키워드를 자연스럽게 포함함",
            "중복 단어를 최소화하고 다양하게 표현",
            "적절한 강조어 사용으로 주목도 높임",
            "문장 길이가 다양하지만 전체적으로 읽기 편함",
            "전문 용어를 과도하지 않게 적절히 사용",
            "경험과 성과를 구체적 표현으로 강조",
            "문장 끝맺음이 명확하여 이해 용이",
            "강조할 부분을 문장 위치로 배치",
            "읽는 사람 관점에서 자연스러운 문장 구성",
            "부정적 표현보다 긍정적 표현 사용",
            "문장에 숫자나 지표를 자연스럽게 포함",
            "주어와 동사를 명확히 하여 의미 분명",
            "한 문장 안에서 메시지를 명확히 전달",
            "적절한 단락 구분으로 가독성 향상",
            "행동 중심 동사 사용으로 역동적 표현",
            "중복되는 표현을 피하고 다양한 표현 사용",
            "문장마다 핵심 키워드 포함",
            "표현이 자연스럽고 읽기 흐름이 부드러움",
            "경험과 성과 강조를 위해 적절한 수식어 사용",
            "결과 중심 서술을 위해 문장 구조 조정",
            "문장 앞부분에 핵심 메시지 배치",
            "읽는 사람이 쉽게 이해할 수 있는 표현 사용",
            "과도한 수식 없이 간결한 표현 유지",
            "자신의 강점을 부드럽게 강조",
            "행동과 결과를 구체적 문장으로 표현",
            "적절한 비유나 예시로 이해 도움",
            "문장 톤이 일관되고 전문적임",
            "표현이 명확하여 평가자가 이해하기 쉬움",
            "중요 메시지를 반복하여 강조",
            "자연스러운 문장 흐름으로 전체 읽기 편함"
        };
        return features[index];
    }

    /**
     * STRUCTURE 카테고리 특징들 (순서대로 선택)
     */
    private String getStructureFeature(int index) {
        String[] features = {
            "자소서 서두에서 지원 동기와 핵심 강점을 명확하게 제시함",
            "경험 중심의 본문이 시간 순서가 아닌 중요도 순으로 구성됨",
            "각 항목별로 소제목을 활용하여 가독성 높음",
            "문단마다 핵심 메시지가 첫 문장에 명확히 드러남",
            "도입, 본문, 결론 구조가 논리적 흐름을 유지함",
            "성과와 경험을 나누어 단계적으로 설명함",
            "핵심 경험을 중심으로 3단 구조를 활용함",
            "첫 문단에서 자신을 한 줄로 요약하는 표현 포함",
            "중간 문단에서 문제-행동-성과(STAR) 구조 활용",
            "각 경험에 대한 배경을 간결하게 서술함",
            "결론에서 지원 직무와 연결되는 요약 제공",
            "문단 길이가 3~5문장으로 일정하게 구성됨",
            "중복 경험 없이 다양한 사례를 균형 있게 제시함",
            "숫자와 통계를 활용해 성과를 구체화함",
            "지원 직무와 관련 없는 경험을 최소화함",
            "논리적 흐름을 위해 접속사를 적절히 사용함",
            "문단 간 연결 문구를 통해 자연스러운 흐름 유지",
            "강점과 개선 경험을 명확히 분리함",
            "자기소개서 길이가 적절하게 분배됨",
            "중요 내용은 문장 시작에 배치함",
            "각 경험별 결과를 명확히 언급함",
            "핵심 메시지를 반복하며 강조함",
            "문장 간 논리적 연결성을 강조함",
            "경험의 배경과 맥락을 충분히 제공함",
            "결과와 배운 점을 한 문장으로 요약함",
            "문단별 중심 문장이 명확함",
            "중요 성과를 강조하기 위해 문단 앞부분에 위치",
            "지원 직무와 직접 연관된 경험만 포함",
            "각 문단마다 핵심 키워드를 포함",
            "도입부에서 전체 구조를 간략히 안내함",
            "논리적 순서로 경험과 성과를 배열함",
            "STAR 구조를 대부분 경험에 적용함",
            "마무리 문단에서 강점과 지원 동기 재강조",
            "자기소개서 전체 길이가 2~3문단으로 균형적"
        };
        return features[index];
    }

    /**
     * CONTENT 카테고리 특징들 (순서대로 선택)
     */
    private String getContentFeature(int index) {
        String[] features = {
            "팀 프로젝트에서 맡은 역할과 구체적 성과를 명시함",
            "문제 해결 과정과 행동을 구체적으로 기술함",
            "성과 지표나 수치로 결과를 구체화함",
            "업무 관련 핵심 경험 위주로 구성함",
            "지원 직무와 관련 있는 프로젝트 중심으로 작성",
            "문제 상황과 해결 방법을 단계적으로 기술함",
            "성과 중심으로 경험을 압축하여 표현함",
            "팀 내 기여도를 명확하게 기술함",
            "실제 사례 기반으로 구체적인 경험 서술",
            "책임과 역할을 분명히 구분하여 표현",
            "직무 역량과 연결되는 경험 강조",
            "성공/실패 경험 모두에서 배운 점 명시",
            "협업 경험과 팀 기여를 구체적으로 표현",
            "문제 해결 능력을 보여주는 경험 포함",
            "업무 성과와 직무 역량을 연계함",
            "프로젝트 목표와 결과를 명확히 연결",
            "전문 기술을 활용한 사례 구체화",
            "성과를 달성한 과정과 수치를 상세히 작성",
            "자발적 개선 및 혁신 사례 포함",
            "업무 효율화 경험과 구체적 방법 기술",
            "직무 관련 자격이나 기술 경험 명시",
            "문제 해결 과정의 단계별 행동 설명",
            "성과 중심으로 경험을 압축하여 표현",
            "팀워크와 소통 능력을 보여주는 사례",
            "지원 직무 핵심 역량과 연계된 경험",
            "자발적 프로젝트 참여 경험 명시",
            "성과 달성 과정에서의 핵심 행동 강조",
            "문제 상황에서 주도적 역할 수행 사례",
            "업무 난이도와 해결 방식을 구체적 설명",
            "기술/역량 적용 사례를 명확히 제시",
            "업무 과정에서 개선 사항을 반영한 경험",
            "지원 직무 관련 문제 해결 경험 강조",
            "프로젝트 성과와 팀 기여도를 수치화"
        };
        return features[index];
    }
}
