package com.cvmento.domain.resume.dto.response;

/**
 * Enum 옵션 응답.
 *
 * @param value 값
 * @param description 설명
 */
public record EnumOptionResponse(
        String value,
        String description
) {
}