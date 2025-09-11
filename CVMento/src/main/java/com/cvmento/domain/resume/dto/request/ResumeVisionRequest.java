package com.cvmento.domain.resume.dto.request;

import java.util.List;

public record ResumeVisionRequest(
        String model,
        List<VisionMessage> messages
) {
    public static ResumeVisionRequest create(String model, String textPrompt, String base64Image) {
        List<VisionContent> content = List.of(
                VisionContent.createText(textPrompt),
                VisionContent.createImage(base64Image)
        );
        
        VisionMessage message = VisionMessage.createUserMessage(content);
        
        return new ResumeVisionRequest(model, List.of(message));
    }
}