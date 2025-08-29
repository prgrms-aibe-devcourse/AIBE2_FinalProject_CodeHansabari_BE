package com.cvmento.domain.coverLetter.dto.response;

public record FeedbackItem(
        String description,    // 피드백 내용
        String suggestion     // 개선 방법
) {}