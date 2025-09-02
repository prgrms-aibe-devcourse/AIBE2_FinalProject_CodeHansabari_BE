package com.cvmento.domain.coverLetter.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record CoverLetterAiRequest(
        @NotBlank(message = "자소서 내용은 필수입니다.")
        @Size(min = 100, max = 2000, message = "자소서는 100자 이상 2000자 이하로 작성해주세요.")
        String content,

        @NotBlank(message = "지원분야는 필수입니다.")
        @Size(max = 100, message = "지원분야는 100자 이하로 작성해주세요.")
        String jobField,  // 지원분야 (예: "백엔드 개발자", "프론트엔드 개발자", "데이터 분석가")

        @NotNull(message = "경력 년수는 필수입니다.")
        @PositiveOrZero(message = "경력 년수는 0 이상이어야 합니다.")
        Integer experienceYears,  // 경력 년수

        @Size(max = 500, message = "사용자 프롬프트는 500자 이하로 작성해주세요.")
        String customPrompt  // 사용자가 입력하는 추가 요구사항 (선택사항)
) {
        // 총 경력을 문자열로 반환하는 헬퍼 메서드
        public String getTotalExperience() {
                if (experienceYears == 0) {
                        return "신입";
                }
                return experienceYears + "년";
        }
}