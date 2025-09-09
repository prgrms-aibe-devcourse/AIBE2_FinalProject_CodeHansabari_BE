package com.cvmento.domain.resume.entity;

import com.cvmento.domain.member.entity.Member;
import com.cvmento.domain.resume.enums.ResumeSectionType;
import com.cvmento.global.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.Getter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import com.cvmento.domain.resume.enums.RecordStatus;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Table(name = "resumes")
@Where(clause = "status = 'ACTIVE'")
public class Resume extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "resume_id")
    private Long resumeId;

    @Column(name = "title", nullable = false)
    private String title;

        @Column(name = "template_type")
    private String templateType = "default";

    // New fields for intro
    @Column(name = "self_introduction", columnDefinition = "TEXT") // Use TEXT for potentially long strings
    private String selfIntroduction;

    @Column(name = "tech_stack") // Store as comma-separated string
    private String techStack;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @OneToMany(mappedBy = "resume", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ResumeSection> sections = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private RecordStatus status = RecordStatus.ACTIVE; // 상태 (활성, 비활성, 삭제)

    protected Resume() {}

    public Resume(String title, Member member, String selfIntroduction, String techStack) { // Updated constructor
        this.title = title;
        this.member = member;
        this.templateType = "default"; // 기본값 설정
        this.selfIntroduction = selfIntroduction; // Set new field
        this.techStack = techStack; // Set new field
    }

    public void updateTitle(String title) {
        this.title = title;
    }

    // New method to update intro fields
    public void updateIntro(String selfIntroduction, String techStack) {
        this.selfIntroduction = selfIntroduction;
        this.techStack = techStack;
    }

    // 연관관계 편의 메소드
    public void addSection(ResumeSectionType sectionType, String sectionTitle, String contentText) {
        ResumeSection section = new ResumeSection(sectionType, sectionTitle, contentText, this);
        this.sections.add(section);
    }

    public void setStatus(RecordStatus status) {
        this.status = status;
    }

    // 기술스택 처리를 위한 헬퍼 메서드
    // TechStackConverter를 통해 일관된 변환 처리
    public List<String> getTechStackList() {
        // 실제 변환은 서비스 계층에서 TechStackConverter를 통해 처리
        // 엔티티에서는 원본 데이터만 제공
        return List.of(); // 임시 - 실제로는 서비스에서 converter 사용
    }

    public void setTechStackFromList(List<String> techStackList) {
        // 실제 변환은 서비스 계층에서 TechStackConverter를 통해 처리
        // 엔티티에서는 변환된 결과만 저장
        this.techStack = null; // 임시 - 실제로는 서비스에서 converter 사용
    }
}


    