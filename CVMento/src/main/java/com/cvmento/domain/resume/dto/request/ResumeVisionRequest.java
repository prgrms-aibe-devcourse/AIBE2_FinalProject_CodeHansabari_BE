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
        // 시스템 메시지: 고정된 프롬프트 (사용자가 조작할 수 없음)
        VisionMessage systemMessage = VisionMessage.createSystemMessage(
                List.of(VisionContent.createText(textPrompt))
        );

        // 유저 메시지: 사용자가 업로드한 이미지 (사용자 입력)
        VisionMessage userMessage = VisionMessage.createUserMessage(
                List.of(VisionContent.createImage(base64Image))
        );

        return new ResumeVisionRequest(model, List.of(systemMessage, userMessage));
    }
}