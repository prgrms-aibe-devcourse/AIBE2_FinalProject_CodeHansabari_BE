package com.cvmento.domain.coverLetter.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 자소서 수정 요청 DTO
 *
 * @param title            수정할 제목
 * @param content          수정할 본문
 * @param jobField         지원 분야
 * @param experienceYears  경력 년수
 */
@Schema(description = "자소서 수정 요청 DTO")
public record CoverLetterUpdateRequest(
        @Schema(description = "자소서 제목", example = "네이버 백엔드 개발자 지원")
        @NotBlank(message = "제목은 필수입니다.")
        @Size(max = 200, message = "제목은 200자 이내로 입력해주세요.")
        String title,

        @Schema(description = "자소서 내용", example = "저는 소프트웨어 개발에 대한 열정을 바탕으로...")
        @NotBlank(message = "내용은 필수입니다.")
        @Size(max = 10000, message = "내용은 10000자 이내로 입력해주세요.")
        String content,

        @Schema(description = "지원분야", example = "백엔드 개발자")
        @Size(max = 100, message = "지원분야는 100자 이내로 입력해주세요.")
        String jobField,

        @Schema(description = "경력 년수 (0: 신입)", example = "1")
        @Min(value = 0, message = "경력 년수는 0 이상이어야 합니다.")
        @Max(value = 50, message = "경력 년수는 50 이하여야 합니다.")
        Integer experienceYears
) {}
