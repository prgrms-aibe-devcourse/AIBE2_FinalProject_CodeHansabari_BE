package com.cvmento.domain.coverLetter.dto.response;

/**
 * 개별 피드백 항목
 *
 * @param description 피드백 내용
 * @param suggestion  개선 방법
 */
public record FeedbackItem(
        String description,
        String suggestion
) {}
