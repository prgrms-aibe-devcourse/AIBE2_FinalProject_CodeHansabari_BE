package com.cvmento.domain.resume.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * LLM 응답을 파싱하기 위한 중간 DTO
 */
@Schema(description = "LLM 이력서 제안 응답 파싱용 DTO")
public record LlmResumeResponse(
        
        @JsonProperty("suggestedResume")
        @Schema(description = "제안된 이력서 정보")
        LlmSuggestedResume suggestedResume,
        
        @JsonProperty("improvementTips")
        @Schema(description = "개선 팁 목록")
        List<String> improvementTips,
        
        @JsonProperty("missingElements")
        @Schema(description = "부족한 요소 목록")
        List<String> missingElements
        
) {
}