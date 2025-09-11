package com.cvmento.domain.resume.dto.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

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