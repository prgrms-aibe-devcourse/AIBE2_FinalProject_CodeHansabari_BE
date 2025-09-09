package com.cvmento.domain.resume.entity;

import com.cvmento.domain.resume.enums.ResumeSectionType;
import com.cvmento.global.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.Getter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import com.cvmento.domain.resume.enums.RecordStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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

    @OneToMany(mappedBy = "section", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ResumeItem> items = new ArrayList<>();

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

    // 연관관계 편의 메소드
    public void addItem(String title, String subTitle, LocalDate startDate, LocalDate endDate, String description) {
        ResumeItem item = new ResumeItem(title, subTitle, startDate, endDate, description, this);
        this.items.add(item);
    }

    public void setStatus(RecordStatus status) {
        this.status = status;
    }

}