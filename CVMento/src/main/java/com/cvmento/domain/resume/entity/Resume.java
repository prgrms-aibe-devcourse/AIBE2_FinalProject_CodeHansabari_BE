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
    private List<ResumeSection> sections = new ArrayList<>();

    protected Resume() {}

    public Resume(String title, Member member) {
        this.title = title;
        this.member = member;
        this.templateType = "default"; // 기본값 설정
    }

    public void updateTitle(String title) {
        this.title = title;
    }

    // 연관관계 편의 메소드
    public void addSection(String sectionType, String sectionTitle, String contentText) {
        ResumeSection section = new ResumeSection(sectionType, sectionTitle, contentText, this);
        this.sections.add(section);
    }
}