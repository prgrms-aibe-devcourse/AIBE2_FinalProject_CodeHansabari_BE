package com.cvmento.domain.resume.dto.request;

import com.cvmento.domain.resume.enums.ResumeSectionType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.util.List;

public record ResumeCreateRequest(
    @NotBlank(message = "이력서 제목은 필수입니다.")
    String title,

    @NotNull(message = "회원 정보는 필수입니다.")
    @Valid
    MemberInfoRequest memberInfo,

    @NotNull(message = "소개 정보는 필수입니다.") // New validation
    @Valid // New validation
    IntroRequest intro, // New field

    @Valid
    List<ResumeSectionRequest> sections
) {
    @Builder
    public ResumeCreateRequest {}

    public record MemberInfoRequest(
        @NotBlank(message = "이름은 필수입니다.")
        String name,

        @NotBlank(message = "이메일은 필수입니다.")
        @Email(message = "유효한 이메일 형식이 아닙니다.")
        String email,

        String phoneNumber,
        String blogUrl
    ) {
        @Builder
        public MemberInfoRequest {}
    }

    // New record for Intro
    public record IntroRequest(
        @NotBlank(message = "자기소개는 필수입니다.")
        String selfIntroduction,
        List<String> techStack // techStack can be empty, so no @NotNull
    ) {
        @Builder
        public IntroRequest {}
    }

    public record ResumeSectionRequest(
        @NotNull(message = "섹션 타입은 필수입니다.")
        ResumeSectionType sectionType,

        @NotBlank(message = "섹션 제목은 필수입니다.")
        String sectionTitle,

        @Valid
        List<SectionItemRequest> items
    ) {
        @Builder
        public ResumeSectionRequest {}
    }

    public record SectionItemRequest(
        @NotBlank(message = "항목 제목은 필수입니다.")
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
