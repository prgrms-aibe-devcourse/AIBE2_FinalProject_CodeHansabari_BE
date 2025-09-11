package com.cvmento.domain.resume.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.multipart.MultipartFile;

@Schema(description = "이력서 파일 변환 요청")
public record ResumeImportRequest(
        @Schema(description = "업로드할 이력서 파일 (PDF, 이미지)", required = true)
        @NotNull(message = "파일은 필수입니다.")
        MultipartFile file
) {
}