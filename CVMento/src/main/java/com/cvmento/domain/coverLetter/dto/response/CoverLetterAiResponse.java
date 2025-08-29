package com.cvmento.domain.coverLetter.dto.response;

public record CoverLetterAiResponse(
        CoverLetterFeedback feedback,
        String improvedContent
) {}