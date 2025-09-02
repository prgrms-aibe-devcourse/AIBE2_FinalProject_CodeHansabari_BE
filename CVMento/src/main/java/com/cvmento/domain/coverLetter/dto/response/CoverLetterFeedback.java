package com.cvmento.domain.coverLetter.dto.response;

import java.util.List;

public record CoverLetterFeedback(
        List<FeedbackItem> strengths,        // 잘한 점
        List<FeedbackItem> improvements,     // 개선이 필요한 점
        String summary
) {}