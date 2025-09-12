package com.cvmento.domain.coverLetter.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 크롤링된 자소서 데이터 수정 요청 DTO
 *
 * @param text 수정할 자소서 본문
 */
public record UpdateCrawlCoverLetterRequest(
        @NotBlank(message = "자소서 내용은 필수입니다.")
        @Size(max = 10000, message = "자소서 내용은 10,000자를 초과할 수 없습니다.")
        String text
) {}
