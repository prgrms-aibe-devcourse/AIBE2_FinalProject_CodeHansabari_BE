package com.cvmento.domain.coverLetter.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CoverLetterAiRequest(
        @NotBlank(message = "자소서 내용은 필수입니다.")
        @Size(min = 100, max = 2000, message = "자소서는 100자 이상 2000자 이하로 작성해주세요.")
        String content,

        @Size(max = 500, message = "사용자 프롬프트는 500자 이하로 작성해주세요.")
        String customPrompt  // 사용자가 입력하는 추가 요구사항 (선택사항)
) {}
