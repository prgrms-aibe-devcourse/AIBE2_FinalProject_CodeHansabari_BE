package com.cvmento.domain.coverLetter.dto.response;

import java.util.List;

/**
 * 자소서 피드백 DTO
 *
 * @param strengths     잘한 점 목록
 * @param improvements  개선 필요 항목
 * @param summary       요약 피드백
 */
public record CoverLetterFeedback(
        List<FeedbackItem> strengths,
        List<FeedbackItem> improvements,
        String summary
) {}
