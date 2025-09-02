package com.cvmento.domain.resume.dto.response;

import com.cvmento.domain.member.entity.Member;
import com.cvmento.domain.resume.entity.Resume;
import com.cvmento.domain.resume.entity.ResumeSection;

import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public record ResumeResponse(
    Long id,
    String title,
    MemberInfoResponse memberInfo,
    List<ResumeSectionResponse> sections,
    String createdAt,
    String updatedAt
) {
    public static ResumeResponse from(Resume resume) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return new ResumeResponse(
                resume.getResumeId(),
                resume.getTitle(),
                MemberInfoResponse.from(resume.getMember()),
                resume.getSections().stream()
                        .map(ResumeSectionResponse::from)
                        .collect(Collectors.toList()),
                formatter.format(resume.getCreatedAt()),
                formatter.format(resume.getUpdatedAt())
        );
    }

    public record MemberInfoResponse(
        String name,
        String email
    ) {
        public static MemberInfoResponse from(Member member) {
            return new MemberInfoResponse(member.getName(), member.getEmail());
        }
    }

    public record ResumeSectionResponse(
        String sectionType,
        String sectionTitle,
        List<SectionItemResponse> items
    ) {
        public static ResumeSectionResponse from(ResumeSection section) {
            // 현재 contentText는 단일 문자열이므로, 파싱하지 않고 하나의 아이템으로 표현합니다.
            // 추후 JSON 등으로 구조화된 데이터를 저장하게 되면 이 부분을 수정해야 합니다.
            SectionItemResponse item = new SectionItemResponse(null, null, null, null, section.getContentText());
            return new ResumeSectionResponse(
                    section.getSectionType(),
                    section.getSectionTitle(),
                    Collections.singletonList(item)
            );
        }
    }

    public record SectionItemResponse(
        String title,
        String subTitle,
        String startDate,
        String endDate,
        String description
    ) {}
}
