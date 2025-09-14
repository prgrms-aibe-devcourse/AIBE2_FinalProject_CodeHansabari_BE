package com.cvmento.domain.interview.dto.response;

import java.util.List;

/**
 * 인터뷰 QnA 목록 응답(집계 정보 포함).
 *
 * @param qnaList        QnA 목록
 * @param totalCount     전체 QnA 수
 * @param generatedCount 새로 생성된 QnA 수
 */
public record InterviewQnaListResponse(
        List<InterviewQnaResponse> qnaList,
        int totalCount,
        int generatedCount
) { }
