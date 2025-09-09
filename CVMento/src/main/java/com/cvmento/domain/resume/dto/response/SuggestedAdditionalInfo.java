package com.cvmento.domain.resume.dto.response;

import com.cvmento.domain.resume.enums.AdditionalInfoCategory;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

@Schema(description = "AI가 제안하는 기타사항 정보")
public record SuggestedAdditionalInfo(

        @Schema(description = "카테고리", example = "CERTIFICATE")
        AdditionalInfoCategory category,

        @Schema(description = "제목", example = "정보처리기사")
        String title,

        @Schema(description = "내용", example = "한국산업인력공단 발행")
        String content,

        @Schema(description = "취득일 또는 날짜", example = "2023-05-15")
        LocalDate achievementDate,

        @Schema(description = "설명", example = "전공 지식을 바탕으로 취득한 국가기술자격증")
        String description

) {
}