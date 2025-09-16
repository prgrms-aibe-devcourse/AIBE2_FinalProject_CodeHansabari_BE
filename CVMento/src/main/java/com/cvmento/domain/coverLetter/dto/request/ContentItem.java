package com.cvmento.domain.coverLetter.dto.request;

/**
 * Responses API 컨텐츠 항목
 */
public record ContentItem(
        String type,    // "input_text", "input_image" 등
        String text     // 텍스트 내용 (input_text일 때)
) {
    // 텍스트 생성용 편의 메서드
    public static ContentItem text(String text) {
        return new ContentItem("input_text", text);
    }
}