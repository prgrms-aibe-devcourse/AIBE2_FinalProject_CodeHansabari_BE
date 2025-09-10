package com.cvmento.domain.resume.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "커스텀 링크 저장 요청")
public record CustomLinkSaveRequest(

        @Schema(description = "링크명", example = "포트폴리오")
        String name,

        @Schema(description = "링크 URL", example = "https://portfolio.example.com")
        String url
) {
}