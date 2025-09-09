package com.cvmento.domain.resume.entity;

import com.cvmento.domain.resume.enums.RecordStatus;
import com.cvmento.global.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.Getter;
import org.hibernate.annotations.Where;

import java.time.LocalDate;

@Entity
@Getter
@Table(name = "resume_items")
@Where(clause = "status = 'ACTIVE'")
public class ResumeItem extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "item_id")
    private Long itemId;

    @Column(name = "title")
    private String title;

    @Column(name = "sub_title")
    private String subTitle;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "section_id", nullable = false)
    private ResumeSection section;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private RecordStatus status = RecordStatus.ACTIVE;

    protected ResumeItem() {}

    public ResumeItem(String title, String subTitle, LocalDate startDate, LocalDate endDate, String description, ResumeSection section) {
        this.title = title;
        this.subTitle = subTitle;
        this.startDate = startDate;
        this.endDate = endDate;
        this.description = description;
        this.section = section;
    }

    public void updateContent(String title, String subTitle, LocalDate startDate, LocalDate endDate, String description) {
        this.title = title;
        this.subTitle = subTitle;
        this.startDate = startDate;
        this.endDate = endDate;
        this.description = description;
    }

    public void setStatus(RecordStatus status) {
        this.status = status;
    }
}