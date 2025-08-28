package com.cvmento.domain.resume.entity;

import com.cvmento.domain.member.entity.Member;
import com.cvmento.global.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Table(name = "resumes")
public class Resume extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "resume_id")
    private Long resumeId;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "template_type")
    private String templateType = "default";

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @OneToMany(mappedBy = "resume", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ResumeSection> resumeSections = new ArrayList<>();

    protected Resume() {}

    public Resume(String title, String templateType, Member member) {
        this.title = title;
        this.templateType = templateType;
        this.member = member;
    }

    public void updateTitle(String title) {
        this.title = title;
    }

    public void updateTemplateType(String templateType) {
        this.templateType = templateType;
    }

    public void addResumeSection(ResumeSection resumeSection) {
        this.resumeSections.add(resumeSection);
        resumeSection.setResume(this);
    }
}