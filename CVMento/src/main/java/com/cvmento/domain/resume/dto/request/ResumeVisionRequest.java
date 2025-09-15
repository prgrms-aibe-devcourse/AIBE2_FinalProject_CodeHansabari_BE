package com.cvmento.domain.resume.dto.request;

import java.util.List;

/**
 * 이력서 Vision API 요청.
 *
 * @param model 사용할 Vision 모델명
 * @param messages Vision API 메시지 목록
 */
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