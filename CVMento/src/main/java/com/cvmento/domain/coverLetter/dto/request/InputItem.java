package com.cvmento.domain.coverLetter.dto.request;

import java.util.List;

/**
 * Responses API 입력 항목
 */
public record InputItem(
        String role,                    // "system" 또는 "user"
        List<ContentItem> content       // 컨텐츠 배열
) {}