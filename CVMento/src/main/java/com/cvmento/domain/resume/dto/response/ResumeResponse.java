package com.cvmento.domain.resume.dto.response;

import com.cvmento.domain.member.entity.Member;
import com.cvmento.domain.resume.entity.Resume;
import com.cvmento.domain.resume.entity.ResumeSection;
import com.cvmento.domain.resume.entity.ResumeItem;
import com.cvmento.domain.resume.enums.ResumeSectionType;

import java.time.format.DateTimeFormatter;
import java.util.Arrays; // New import
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public record ResumeResponse(
    Long id,
    String title,
    MemberInfoResponse memberInfo,
    IntroResponse intro, // New field
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
                IntroResponse.from(resume.getSelfIntroduction(), resume.getTechStackList()), // Use new method
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

    // New record for IntroResponse
    public record IntroResponse(
        String selfIntroduction,
        List<String> techStack
    ) {
        public static IntroResponse from(String selfIntroduction, List<String> techStackList) {
            return new IntroResponse(selfIntroduction, techStackList != null ? techStackList : Collections.emptyList());
        }
    }

    public record ResumeSectionResponse(
        String sectionType,
        String sectionTitle,
        List<SectionItemResponse> items
    ) {
        public static ResumeSectionResponse from(ResumeSection section) {
            List<SectionItemResponse> items;
            
            if (section.getItems().isEmpty()) {
                // 기존 contentText 기반 (하위 호환성)
                SectionItemResponse item = new SectionItemResponse(null, null, null, null, section.getContentText());
                items = Collections.singletonList(item);
            } else {
                // 새로운 구조화된 items 사용
                items = section.getItems().stream()
                    .map(SectionItemResponse::from)
                    .collect(Collectors.toList());
            }
            
            return new ResumeSectionResponse(
                    section.getSectionType().name(),
                    section.getSectionTitle(),
                    items
            );
        }
    }

    public record SectionItemResponse(
        String title,
        String subTitle,
        String startDate,
        String endDate,
        String description
    ) {
        public static SectionItemResponse from(ResumeItem item) {
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            return new SectionItemResponse(
                item.getTitle(),
                item.getSubTitle(),
                item.getStartDate() != null ? item.getStartDate().format(dateFormatter) : null,
                item.getEndDate() != null ? item.getEndDate().format(dateFormatter) : null,
                item.getDescription()
            );
        }
    }
}
