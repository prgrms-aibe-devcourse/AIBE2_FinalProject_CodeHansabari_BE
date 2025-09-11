package com.cvmento.domain.resume.dto.request;

import java.util.List;

public record VisionMessage(
        String role,
        List<VisionContent> content
) {
    public static VisionMessage createUserMessage(List<VisionContent> content) {
        return new VisionMessage("user", content);
    }
}