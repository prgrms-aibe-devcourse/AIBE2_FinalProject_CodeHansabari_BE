package com.cvmento.domain.resume.dto.request;

import java.util.List;

/**
 * Vision API 메시지.
 *
 * @param role 메시지 역할 (system, user, assistant)
 * @param content 메시지 컨텐츠 목록
 */
public record VisionMessage(
        String role,
        List<VisionContent> content
) {
    public static VisionMessage createSystemMessage(List<VisionContent> content) {
        return new VisionMessage("system", content);
    }

    public static VisionMessage createUserMessage(List<VisionContent> content) {
        return new VisionMessage("user", content);
    }
}