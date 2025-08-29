package com.cvmento.domain.resume.dto.request;

import lombok.Builder;

import java.util.List;

// ResumeCreateRequest와 구조가 동일하지만, 명확한 API 분리를 위해 별도 파일로 관리합니다.
public record ResumeUpdateRequest(
    String title,
    MemberInfoRequest memberInfo,
    List<ResumeSectionRequest> sections
) {
    @Builder
    public ResumeUpdateRequest {}

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
