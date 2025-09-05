package com.cvmento.domain.coverLetter.dto.response;

public record EvidenceDto(
    Long essayId,
    Long chunkId,
    int charStart,
    int charEnd,
    String text
) {}
