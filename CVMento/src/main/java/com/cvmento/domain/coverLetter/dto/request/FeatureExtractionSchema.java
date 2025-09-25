package com.cvmento.domain.coverLetter.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

/**
 * Gemini API JSON 구조화된 응답을 위한 스키마 정의
 * 
 * 이 스키마는 Gemini 2.5 Flash API에 전달되어 일관된 JSON 응답을 보장합니다.
 * 
 * 응답 형식:
 * {
 *   "features": [
 *     {
 *       "feature_category": "EXPRESSION|STRUCTURE|CONTENT",
 *       "description": "특징을 한 문장으로 설명"
 *     }
 *   ]
 * }
 */
@Slf4j
public class FeatureExtractionSchema {
    
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * Gemini API에 전달할 JSON Schema
     * 이 스키마는 Gemini가 응답을 생성할 때 반드시 따라야 하는 구조를 정의합니다.
     */
    public static final JsonNode SCHEMA = createSchema();
    
    /**
     * JSON Schema 생성
     * Gemini API의 responseSchema 파라미터에 전달됩니다.
     */
    private static JsonNode createSchema() {
        try {
            String schemaJson = """
                {
                  "type": "object",
                  "properties": {
                    "features": {
                      "type": "array",
                      "items": {
                        "type": "object",
                        "properties": {
                          "feature_category": {
                            "type": "string",
                            "enum": ["EXPRESSION", "STRUCTURE", "CONTENT"],
                            "description": "특징 카테고리"
                          },
                          "description": {
                            "type": "string",
                            "description": "특징을 한 문장으로 설명 (100자 이내)"
                          }
                        },
                        "required": ["feature_category", "description"]
                      }
                    }
                  },
                  "required": ["features"]
                }
                """;
            
            JsonNode schema = objectMapper.readTree(schemaJson);
            log.debug("FeatureExtractionSchema 생성 완료");
            return schema;
            
        } catch (Exception e) {
            log.error("FeatureExtractionSchema 생성 실패", e);
            // 기본 스키마 반환
            try {
                return objectMapper.readTree("""
                    {
                      "type": "object",
                      "properties": {
                        "features": {
                          "type": "array",
                          "items": {
                            "type": "object",
                            "properties": {
                              "feature_category": {"type": "string"},
                              "description": {"type": "string"}
                            },
                            "required": ["feature_category", "description"]
                          }
                        }
                      },
                      "required": ["features"]
                    }
                    """);
            } catch (Exception fallbackError) {
                log.error("기본 스키마 생성도 실패", fallbackError);
                return objectMapper.createObjectNode();
            }
        }
    }
    
    /**
     * 스키마 검증을 위한 응답 모델
     * 실제 응답이 이 구조를 따르는지 확인할 수 있습니다.
     */
    public static class FeatureExtractionResponse {
        @JsonProperty("features")
        private java.util.List<Feature> features;
        
        public static class Feature {
            @JsonProperty("feature_category")
            private String featureCategory;
            
            @JsonProperty("description")
            private String description;
            
            // Getters and Setters
            public String getFeatureCategory() { return featureCategory; }
            public void setFeatureCategory(String featureCategory) { this.featureCategory = featureCategory; }
            public String getDescription() { return description; }
            public void setDescription(String description) { this.description = description; }
        }
        
        // Getters and Setters
        public java.util.List<Feature> getFeatures() { return features; }
        public void setFeatures(java.util.List<Feature> features) { this.features = features; }
    }
    
    /**
     * 스키마 정보 출력 (디버깅용)
     */
    public static void printSchema() {
        try {
            log.info("FeatureExtractionSchema: {}", objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(SCHEMA));
        } catch (Exception e) {
            log.error("스키마 출력 실패", e);
        }
    }
}
