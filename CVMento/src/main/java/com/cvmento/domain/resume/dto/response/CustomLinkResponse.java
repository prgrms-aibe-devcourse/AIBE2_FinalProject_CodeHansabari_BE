package com.cvmento.domain.resume.dto.response;

/**
 * 커스텀 링크 응답.
 *
 * @param name 링크명
 * @param url 링크 URL
 */
public record CustomLinkResponse(
        String name,
        String url
) {
}