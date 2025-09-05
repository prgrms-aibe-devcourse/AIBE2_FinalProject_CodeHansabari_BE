package com.cvmento.domain.resume.dto.llm;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

import java.util.List;

@Builder
public record LlmVisionRequest(
    String model,
    List<Message> input
    
) {
    @Builder
    public record Message(
        String role,
        List<Content> content
    ) {}

    public interface Content {}

    @Builder
    public record TextContent(
        String type,
        String text
    ) implements Content {}

    @Builder
    public record ImageUrlContent(
        String type,
        @JsonProperty("image_url")
        ImageUrl imageUrl
    ) implements Content {}

    @Builder
    public record ImageUrl(
        String url,
        String detail
    ) {
        public ImageUrl(String url) {
            this(url, "auto");
        }
    }
}
