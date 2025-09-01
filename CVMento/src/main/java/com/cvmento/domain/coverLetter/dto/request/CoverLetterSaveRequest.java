package com.cvmento.domain.coverLetter.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CoverLetterSaveRequest(
        @NotBlank(message = "제목은 필수입니다.")
        @Size(max = 100, message = "제목은 100자 이하로 작성해주세요.")
        String title,

        @NotBlank(message = "내용은 필수입니다.")
        @Size(min = 100, max = 2000, message = "내용은 50자 이상 2000자 이하로 작성해주세요.")
        String content,

        boolean isAiImproved  // AI 첨삭 여부
) {}