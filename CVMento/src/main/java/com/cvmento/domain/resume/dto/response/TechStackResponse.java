package com.cvmento.domain.resume.dto.response;

import com.cvmento.domain.resume.entity.TechStack;

public record TechStackResponse(
        Long id,
        String name,
        String category
) {
    public static TechStackResponse from(TechStack techStack) {
        return new TechStackResponse(
                techStack.getId(),
                techStack.getName(),
                techStack.getCategory()
        );
    }
}