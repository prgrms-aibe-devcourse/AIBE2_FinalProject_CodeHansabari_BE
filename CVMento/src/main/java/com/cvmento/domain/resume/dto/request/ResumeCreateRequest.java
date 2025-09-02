package com.cvmento.domain.resume.dto.request;

import java.util.List;
import lombok.Builder;

public record ResumeCreateRequest(
    String title,
    MemberInfoRequest memberInfo,
    List<ResumeSectionRequest> sections
) {
    @Builder
    public ResumeCreateRequest {}

    public record MemberInfoRequest(
        String name,
        String email,
        String phoneNumber,
        String blogUrl
    ) {
        @Builder
        public MemberInfoRequest {}
    }

    public record ResumeSectionRequest(
        String sectionType,
        String sectionTitle,
        List<SectionItemRequest> items
    ) {
        @Builder
        public ResumeSectionRequest {}
    }

    public record SectionItemRequest(
        String title,
        String subTitle,
        String startDate,
        String endDate,
        String description
    ) {
        @Builder
        public SectionItemRequest {}
    }
}
