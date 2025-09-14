package com.cvmento.domain.coverLetter.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

/**
 * 자소서 저장 요청 DTO
 *
 * @param title            자소서 제목 (100자 이하)
 * @param content          자소서 본문 (100~2000자)
 * @param jobField         지원 분야
 * @param experienceYears  경력 년수 (0 이상)
 * @param isAiImproved     AI 첨삭 여부
 */
public record CoverLetterSaveRequest(
        @NotBlank(message = "제목은 필수입니다.")
        @Size(max = 100, message = "제목은 100자 이하로 작성해주세요.")
        String title,

        @NotBlank(message = "내용은 필수입니다.")
        @Size(min = 100, max = 2000, message = "내용은 100자 이상 2000자 이하로 작성해주세요.")
        String content,

        @NotBlank(message = "지원분야는 필수입니다.")
        @Size(max = 100, message = "지원분야는 100자 이하로 작성해주세요.")
        String jobField,

        @NotNull(message = "경력 년수는 필수입니다.")
        @PositiveOrZero(message = "경력 년수는 0 이상이어야 합니다.")
        Integer experienceYears,

        boolean isAiImproved
) {}
