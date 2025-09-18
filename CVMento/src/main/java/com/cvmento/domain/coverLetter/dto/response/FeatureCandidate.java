package com.cvmento.domain.coverLetter.dto.response;


public record FeatureCandidate(
    String featureCategory,  // "EXPRESSION", "STRUCTURE", "CONTENT"
    String description,  // 특징을 한 문장으로 설명
    Long crawlCoverLetterId  // 해당 특징이 추출된 자소서의 ID
) {}
