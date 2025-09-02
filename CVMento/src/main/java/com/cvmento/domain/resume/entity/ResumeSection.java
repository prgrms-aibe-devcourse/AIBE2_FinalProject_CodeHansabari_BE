package com.cvmento.domain.resume.entity;

import com.cvmento.domain.resume.enums.ResumeSectionType;
import com.cvmento.global.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.Getter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import com.cvmento.domain.resume.enums.RecordStatus;
import java.time.LocalDateTime;

@Entity
@Getter
@Table(name = "resume_sections")
@Where(clause = "status = 'ACTIVE'")
public class ResumeSection extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "section_id")
    private Long sectionId;

    @Enumerated(EnumType.STRING)
    @Column(name = "section_type", nullable = false)
    private ResumeSectionType sectionType;

    @Column(name = "section_title")
    private String sectionTitle;

    @Column(name = "content_text", nullable = false, columnDefinition = "TEXT")
    private String contentText;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resume_id", nullable = false)
    private Resume resume;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private RecordStatus status = RecordStatus.ACTIVE; // 상태 (활성, 비활성, 삭제)

    protected ResumeSection() {}

    public ResumeSection(ResumeSectionType sectionType, String sectionTitle, String contentText, Resume resume) {
        this.sectionType = sectionType;
        this.sectionTitle = sectionTitle;
        this.contentText = contentText;
        this.resume = resume;
    }

    public void updateContent(String contentText) {
        this.contentText = contentText;
    }

    public void setResume(Resume resume) {
        this.resume = resume;
    }

}