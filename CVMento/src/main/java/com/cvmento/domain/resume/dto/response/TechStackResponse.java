package com.cvmento.domain.resume.dto.response;

import com.cvmento.domain.resume.entity.TechStack;

/**
 * 기술스택 응답.
 *
 * @param id 기술스택 ID
 * @param name 기술스택명
 * @param category 카테고리
 */
public record TechStackResponse(
        Long id,
        String name,
        String category
) {
    /**
     * Entity에서 Response로 변환
     */
    public static TechStackResponse from(TechStack techStack) {
        return new TechStackResponse(
                techStack.getId(),
                techStack.getName(),
                techStack.getCategory()
        );
    }
}