package com.cvmento.domain.coverLetter.dto.response;

public record EssayChunk(
    Long essayId,
    int chunkIndex,
    String content,
    int charStart,
    int charEnd
) {}
