package com.cvmento.domain.coverLetter.dto.response;

import java.util.List;

public record FeatureCandidate(
    String featureCategory,  // "EXPRESSION", "STRUCTURE", "CONTENT"
    String description  // 특징을 한 문장으로 설명
) {}
