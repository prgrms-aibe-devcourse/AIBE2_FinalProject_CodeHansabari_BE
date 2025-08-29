package com.cvmento.domain.coverLetter.service;

import com.cvmento.domain.coverLetter.dto.internal.CoverLetterFeatureDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CoverLetterLlmPromptService {

    public String buildImprovementPrompt(String content, List<CoverLetterFeatureDto> features) {
        return buildPromptStructure() +
                buildFeatureCriteria(features) +
                buildContentSection(content) +
                buildRequestSection() +
                buildGuidelines();
    }

    private String buildPromptStructure() {
        return """
            당신은 20년 경력의 전문 자소서 컨설턴트입니다.
            주어진 자소서 내용을 분석하고 개선된 버전을 작성해주세요.
            
            """;
    }

    private String buildFeatureCriteria(List<CoverLetterFeatureDto> features) {
        StringBuilder criteria = new StringBuilder("## 우수 자소서 작성 기준\n");

        Map<String, List<CoverLetterFeatureDto>> categoryFeatures =
                features.stream().collect(Collectors.groupingBy(CoverLetterFeatureDto::category));

        categoryFeatures.forEach((category, categoryList) -> {
            criteria.append("### ").append(category).append("\n"); // DTO에 category가 String
            categoryList.forEach(feature ->
                    criteria.append("- ").append(feature.description()).append("\n"));
            criteria.append("\n");
        });

        return criteria.toString();
    }

    private String buildContentSection(String content) {
        return "## 분석할 자소서\n" + content + "\n\n";
    }

    private String buildRequestSection() {
        return """
            ## 작업 요청
            자소서 내용을 바탕으로 지원 직무와 회사를 추론하여 분석해주세요.
            다음 JSON 형식으로 응답해주세요:
            
            ```json
            {
              "feedback": {
                "strengths": [
                  {
                    "description": "잘한 점에 대한 구체적 설명",
                    "suggestion": "더 발전시킬 수 있는 방향"
                  }
                ],
                "improvements": [
                  {
                    "description": "개선이 필요한 점에 대한 구체적 설명",
                    "suggestion": "구체적인 개선 방법"
                  }
                ],
                "summary": "전체 분석 요약"
              },
              "improvedContent": "완전히 개선된 자소서 전문"
            }
            ```
            
            """;
    }

    private String buildGuidelines() {
        return """
            ### 중요 지침
            1. **맥락 파악**: 자소서 내용에서 지원 직무, 업계, 회사 성격을 추론하여 분석
            2. **개선된 자소서**: 원본의 핵심 경험과 성과는 유지하되, 표현과 구조를 대폭 개선
            3. **구체적 피드백**: description과 suggestion만 간단명료하게 작성
            4. **JSON 형식 준수**: 반드시 유효한 JSON 형식으로 응답
            """;
    }

}