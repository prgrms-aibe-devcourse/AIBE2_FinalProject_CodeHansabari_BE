package com.cvmento.domain.coverLetter.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

/**
 * 자소서 AI 첨삭 요청 DTO
 *
 * @param content          자소서 본문 (100~2000자)
 * @param jobField         지원 분야 (예: 백엔드, 프론트엔드)
 * @param experienceYears  경력 년수 (0 이상)
 * @param customPrompt     추가 프롬프트 (선택, 최대 500자)
 */
public record CoverLetterAiRequest(
        @NotBlank(message = "자소서 내용은 필수입니다.")
        @Size(min = 100, max = 2000, message = "자소서는 100자 이상 2000자 이하로 작성해주세요.")
        String content,

        @NotBlank(message = "지원분야는 필수입니다.")
        @Size(max = 100, message = "지원분야는 100자 이하로 작성해주세요.")
        String jobField,

        @NotNull(message = "경력 년수는 필수입니다.")
        @PositiveOrZero(message = "경력 년수는 0 이상이어야 합니다.")
        Integer experienceYears,

        @Size(max = 500, message = "사용자 프롬프트는 500자 이하로 작성해주세요.")
        String customPrompt
) {
        /** 경력을 문자열로 변환 (예: "신입", "3년") */
        public String getTotalExperience() {
                if (experienceYears == 0) {
                        return "신입";
                }
                return experienceYears + "년";
        }
}
