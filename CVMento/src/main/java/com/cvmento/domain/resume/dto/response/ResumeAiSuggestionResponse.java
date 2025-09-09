package com.cvmento.domain.resume.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "이력서 AI 제안 응답")
public record ResumeAiSuggestionResponse(

        @Schema(description = "AI가 제안하는 이력서 내용")
        SuggestedResume suggestedResume,

        @Schema(description = "이력서 작성 팁", 
                example = "[\"경력 사항에 구체적인 숫자와 성과를 포함하세요\", \"프로젝트 설명 시 사용한 기술스택을 명시하세요\"]")
        List<String> improvementTips,

        @Schema(description = "부족한 요소들", 
                example = "[\"프로젝트 성과 지표\", \"교육 이력\", \"자격증 정보\"]")
        List<String> missingElements

) {
}