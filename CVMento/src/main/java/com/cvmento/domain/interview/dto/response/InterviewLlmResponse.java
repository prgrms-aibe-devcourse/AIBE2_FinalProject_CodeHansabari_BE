package com.cvmento.domain.interview.dto.response;

import java.util.List;

public record InterviewLlmResponse(
        List<InterviewQnaDto> qnaList
) {
}