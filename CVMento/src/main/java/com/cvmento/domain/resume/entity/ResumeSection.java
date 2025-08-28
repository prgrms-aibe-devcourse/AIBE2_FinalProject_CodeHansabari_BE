package com.cvmento.domain.resume.entity;

import com.cvmento.global.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Getter
@Table(name = "resume_sections")
public class ResumeSection extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "section_id")
    private Long sectionId;

    @Column(name = "section_type", nullable = false)
    private String sectionType;

    @Column(name = "content_text", nullable = false, columnDefinition = "TEXT")
    private String contentText;

    @Column(name = "ai_recommendation", columnDefinition = "JSON")
    private String aiRecommendation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resume_id", nullable = false)
    private Resume resume;

    protected ResumeSection() {}

    public ResumeSection(String sectionType, String contentText, Resume resume) {
        this.sectionType = sectionType;
        this.contentText = contentText;
        this.resume = resume;
    }

    public void updateContent(String contentText) {
        this.contentText = contentText;
    }

    public void updateAiRecommendation(String aiRecommendation) {
        this.aiRecommendation = aiRecommendation;
    }

    public void setResume(Resume resume) {
        this.resume = resume;
    }
}