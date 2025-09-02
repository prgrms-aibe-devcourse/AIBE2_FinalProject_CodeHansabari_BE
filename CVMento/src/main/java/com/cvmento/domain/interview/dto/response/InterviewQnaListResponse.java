package com.cvmento.domain.interview.dto.response;

import java.util.List;

public record InterviewQnaListResponse(
        List<InterviewQnaResponse> qnaList,
        int totalCount,
        int generatedCount
) {}