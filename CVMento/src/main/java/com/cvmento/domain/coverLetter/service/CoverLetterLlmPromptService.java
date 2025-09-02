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

    public String buildImprovementPrompt(String content, List<CoverLetterFeatureDto> features,
                                         String jobField, String totalExperience, String customPrompt) {
        return buildPromptStructure() +
                buildFeatureCriteria(features) +
                buildApplicantContextSection(jobField, totalExperience) +
                buildContentSection(content) +
                buildCustomPromptSection(customPrompt) +
                buildRequestSection() +
                buildGuidelines();
    }

    // 기존 방식 유지 (하위 호환성)
    public String buildImprovementPrompt(String content, List<CoverLetterFeatureDto> features, String customPrompt) {
        return buildImprovementPrompt(content, features, null, null, customPrompt);
    }

    public String buildImprovementPrompt(String content, List<CoverLetterFeatureDto> features) {
        return buildImprovementPrompt(content, features, null, null, null);
    }

    private String buildPromptStructure() {
        return """
            당신은 20년 경력의 전문 자소서 컨설턴트입니다.
            제공해 드리는 우수한 자소서 작성 기준(특징)을 바탕으로
            지원자의 경력과 지원분야에 맞게 주어진 자소서 내용을 분석하고 개선된 버전을 작성해주세요.
            
            """;
    }

    private String buildFeatureCriteria(List<CoverLetterFeatureDto> features) {
        StringBuilder criteria = new StringBuilder("## 우수 자소서 작성 기준\n");

        Map<String, List<CoverLetterFeatureDto>> categoryFeatures =
                features.stream().collect(Collectors.groupingBy(CoverLetterFeatureDto::category));

        categoryFeatures.forEach((category, categoryList) -> {
            criteria.append("### ").append(category).append("\n");
            categoryList.forEach(feature ->
                    criteria.append("- ").append(feature.description()).append("\n"));
            criteria.append("\n");
        });

        return criteria.toString();
    }

    private String buildApplicantContextSection(String jobField, String totalExperience) {
        if (jobField == null && totalExperience == null) {
            return "";
        }

        StringBuilder context = new StringBuilder("## 지원자 정보\n");

        if (jobField != null && !jobField.trim().isEmpty()) {
            context.append("**지원분야**: ").append(jobField).append("\n");
        }

        if (totalExperience != null && !totalExperience.trim().isEmpty()) {
            context.append("**경력**: ").append(totalExperience).append("\n");
        }

        context.append("\n위 정보를 반드시 고려하여 해당 분야와 경력수준에 맞는 자소서로 개선해주세요.\n\n");

        return context.toString();
    }

    private String buildContentSection(String content) {
        return "## 분석할 자소서\n" + content + "\n\n";
    }

    private String buildCustomPromptSection(String customPrompt) {
        if (customPrompt == null || customPrompt.trim().isEmpty()) {
            return "";
        }

        return """
            ## 추가 요구사항
            사용자가 특별히 요청한 사항을 반영하여 자소서를 개선해주세요:
            """ + customPrompt.trim() + "\n\n";
    }

    private String buildRequestSection() {
        return """
            ## 작업 요청
            지원자의 경력과 지원분야 정보를 바탕으로 자소서를 분석해주세요.
            위의 우수 자소서 작성 기준과 추가 요구사항(있는 경우)을 모두 고려하여 개선해주세요.
            
            **중요**: 원본 자소서가 이미 좋은 품질이라면 무리하게 많은 부분을 바꾸지 말고, 
            정말 개선이 필요한 부분만 수정하여 자연스러운 자소서를 작성해주세요.
            
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
              "improvedContent": "개선된 자소서 전문 (원본이 우수하다면 최소한의 수정만 적용)"
            }
            ```
            
            """;
    }

    private String buildGuidelines() {
        return """
            ### 중요 지침
            1. **맥락 파악**: 지원분야와 경력수준을 반드시 고려하여 분석
            2. **경력 맞춤**: 신입/경력에 따라 강조할 포인트를 다르게 설정
            3. **분야 전문성**: 지원분야에 맞는 전문 용어와 역량을 적절히 사용
            4. **기본 기준 준수**: 우수 자소서 작성 기준을 반드시 반영
            5. **추가 요구사항 반영**: 사용자의 추가 요구사항이 있다면 우선적으로 고려
            6. **개선 원칙**: 
               - 원본이 이미 우수한 품질이라면 과도한 수정을 피하고 필요한 부분만 개선
               - 원본의 핵심 경험과 성과는 유지하되, 표현과 구조를 개선
               - 원본의 내용을 왜곡하거나 삭제하지 않음
               - 좋은 내용은 그대로 유지하고, 부족한 부분만 보완
            7. **구체적 피드백**: description과 suggestion만 간단명료하게 작성
            8. **JSON 형식 준수**: 반드시 유효한 JSON 형식으로 응답
            9. **언어**: 모든 응답은 한국어로 작성
            10. **응답 길이**: 피드백은 간결하게, 개선된 자소서는 2000자 이내로 작성
            """;
    }
}