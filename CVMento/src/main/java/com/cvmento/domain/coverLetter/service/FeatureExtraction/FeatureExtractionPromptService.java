package com.cvmento.domain.coverLetter.service.FeatureExtraction;

import com.cvmento.domain.coverLetter.entity.CrawlCoverLetter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 특징 추출을 위한 프롬프트 생성 서비스
 * - 배치 처리용 프롬프트 생성
 * - 단일 자소서용 프롬프트 생성
 * - 동적 배치 프롬프트 생성
 * - 동적 응답 스키마 생성
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FeatureExtractionPromptService {

    // 삭제: buildBatchExtractionPrompt - 사용처 없음

    /**
     * 단일 자소서 특징 추출용 프롬프트 생성
     */
    public String buildFullCoverLetterExtractionPrompt(CrawlCoverLetter coverLetter) {
        return String.format("""
            당신은 합격 자소서의 관찰 가능한 특징을 3축(표현력/구조/스토리)으로 추출하는 전문 분석가입니다.
            결과는 반드시 유효한 JSON 형식으로만 출력하고, 주어진 스키마를 정확히 준수하세요. 평가나 권고사항, 일반론은 절대 포함하지 마세요.
            
            [메타] crawl_cover_letter_id=%d, 전체_자소서_길이=%d자
            
            [지침]
            - 각 카테고리에서 정확히 1개씩, 총 3개 특징 추출
            - 'feature_category'는 "EXPRESSION"(표현력), "STRUCTURE"(구조), "CONTENT"(이야기) 중 하나
            - 'description'은 특징을 한 문장으로 간결하게 설명 (100자 이내)
            - **중요**: 다른 자소서 작성 시에도 참고할 수 있는 범용적인 특징을 추출하세요
            - 구체적인 내용보다는 일반적인 패턴을 기술하세요
            - 글 내에서 실제로 관찰되는 패턴만 기술하고, 일반적인 조언은 금지
            
            [자소서 전체 내용]
            %s
            
            [JSON 스키마]
            {
              "features": [
                {
                  "feature_category": "EXPRESSION|STRUCTURE|CONTENT",
                  "description": "특징을 한 문장으로 설명"
                }
              ]
            }
            """, coverLetter.getCoverLetterId(), coverLetter.getText().length(), coverLetter.getText());
    }

    /**
     * 동적 배치 프롬프트 생성
     */
    public String buildDynamicBatchPrompt(List<CrawlCoverLetter> batch) {
        StringBuilder prompt = new StringBuilder();
        prompt.append(String.format("다음 %d개 자소서에서 각각의 특징을 추출해주세요:\n\n", batch.size()));
        
        for (int i = 0; i < batch.size(); i++) {
            CrawlCoverLetter coverLetter = batch.get(i);
            prompt.append(String.format("자소서 %d (ID: %d):\n%s\n\n", 
                                       i + 1, 
                                       coverLetter.getCoverLetterId(), 
                                       coverLetter.getText()));
        }
        
        prompt.append("각 자소서별로 EXPRESSION, STRUCTURE, CONTENT 특징을 1개씩 추출해주세요.\n");
        prompt.append("EXPRESSION: 표현 방식의 특징 (구체적 수치, 설득력 있는 표현 등)\n");
        prompt.append("STRUCTURE: 구조적 특징 (논리적 전개, 체계적 구성 등)\n");
        prompt.append("CONTENT: 내용적 특징 (구체적 경험, 성과, 인사이트 등)");
        
        return prompt.toString();
    }

    /**
     * 동적 응답 스키마 생성
     */
    public Object buildDynamicResponseSchema(List<CrawlCoverLetter> batch) {
        Map<String, Object> schema = new HashMap<>();
        schema.put("type", "object");
        
        Map<String, Object> properties = new HashMap<>();
        for (CrawlCoverLetter coverLetter : batch) {
            String key = "cover_letter_" + coverLetter.getCoverLetterId();
            
            Map<String, Object> coverLetterSchema = new HashMap<>();
            coverLetterSchema.put("type", "object");
            
            Map<String, Object> coverLetterProperties = new HashMap<>();
            
            // EXPRESSION 특징
            Map<String, Object> expressionFeature = new HashMap<>();
            expressionFeature.put("type", "object");
            Map<String, Object> expressionProps = new HashMap<>();
            expressionProps.put("feature_category", Map.of("type", "string", "enum", List.of("EXPRESSION")));
            expressionProps.put("description", Map.of("type", "string", "description", "표현 방식 특징을 한 문장으로 설명 (100자 이내)"));
            expressionFeature.put("properties", expressionProps);
            expressionFeature.put("required", List.of("feature_category", "description"));
            coverLetterProperties.put("expression_feature", expressionFeature);
            
            // STRUCTURE 특징
            Map<String, Object> structureFeature = new HashMap<>();
            structureFeature.put("type", "object");
            Map<String, Object> structureProps = new HashMap<>();
            structureProps.put("feature_category", Map.of("type", "string", "enum", List.of("STRUCTURE")));
            structureProps.put("description", Map.of("type", "string", "description", "구조적 특징을 한 문장으로 설명 (100자 이내)"));
            structureFeature.put("properties", structureProps);
            structureFeature.put("required", List.of("feature_category", "description"));
            coverLetterProperties.put("structure_feature", structureFeature);
            
            // CONTENT 특징
            Map<String, Object> contentFeature = new HashMap<>();
            contentFeature.put("type", "object");
            Map<String, Object> contentProps = new HashMap<>();
            contentProps.put("feature_category", Map.of("type", "string", "enum", List.of("CONTENT")));
            contentProps.put("description", Map.of("type", "string", "description", "내용적 특징을 한 문장으로 설명 (100자 이내)"));
            contentFeature.put("properties", contentProps);
            contentFeature.put("required", List.of("feature_category", "description"));
            coverLetterProperties.put("content_feature", contentFeature);
            
            coverLetterSchema.put("properties", coverLetterProperties);
            coverLetterSchema.put("required", List.of("expression_feature", "structure_feature", "content_feature"));
            
            properties.put(key, coverLetterSchema);
        }
        
        schema.put("properties", properties);
        return schema;
    }
}
