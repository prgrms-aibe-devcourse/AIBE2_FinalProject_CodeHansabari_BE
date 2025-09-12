package com.cvmento.domain.resume.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 커스텀 링크 저장 요청.
 *
 * @param name 링크명
 * @param url 링크 URL
 */
@Schema(description = "커스텀 링크 저장 요청")
public record CustomLinkSaveRequest(
        @Schema(description = "링크명", example = "포트폴리오")
        String name,

        @Schema(description = "링크 URL", example = "https://portfolio.example.com")
        String url
) {
}