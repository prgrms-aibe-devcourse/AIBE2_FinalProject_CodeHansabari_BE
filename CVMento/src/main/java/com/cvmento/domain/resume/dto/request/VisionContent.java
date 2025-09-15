package com.cvmento.domain.resume.dto.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Vision API 컨텐츠.
 *
 * @param type 컨텐츠 타입 (text, image_url)
 * @param text 텍스트 내용
 * @param imageUrl 이미지 URL 정보
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record VisionContent(
        String type,
        String text,
        @JsonProperty("image_url") 
        ImageUrl imageUrl
) {
    public static VisionContent createText(String text) {
        return new VisionContent("text", text, null);
    }
    
    public static VisionContent createImage(String base64Url) {
        return new VisionContent("image_url", null, new ImageUrl(base64Url));
    }
    
    /**
     * 이미지 URL 정보.
     *
     * @param url 이미지 URL (Base64 포함)
     * @param detail 이미지 상세도 (high, low)
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record ImageUrl(
            String url,
            String detail
    ) {
        public ImageUrl(String url) {
            this(url, "high");
        }
    }
}