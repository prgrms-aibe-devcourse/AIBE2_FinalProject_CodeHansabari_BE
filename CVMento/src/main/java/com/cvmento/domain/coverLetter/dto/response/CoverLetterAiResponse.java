package com.cvmento.domain.coverLetter.dto.response;

/**
 * 자소서 AI 첨삭 응답 DTO
 *
 * @param feedback        AI 피드백
 * @param improvedContent AI가 개선한 본문
 */
public record CoverLetterAiResponse(
        CoverLetterFeedback feedback,
        String improvedContent
) {}
