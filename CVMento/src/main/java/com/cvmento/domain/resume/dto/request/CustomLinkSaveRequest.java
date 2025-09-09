package com.cvmento.domain.resume.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "커스텀 링크 저장 요청")
public record CustomLinkSaveRequest(

        @Schema(description = "링크명", example = "포트폴리오")
        @NotBlank(message = "링크명은 필수입니다.")
        String name,

        @Schema(description = "링크 URL", example = "https://portfolio.example.com")
        @NotBlank(message = "링크 URL은 필수입니다.")
        String url
) {
}