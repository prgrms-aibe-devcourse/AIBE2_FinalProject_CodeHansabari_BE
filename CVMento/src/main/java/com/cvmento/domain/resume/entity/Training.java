package com.cvmento.domain.resume.entity;

import com.cvmento.global.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "training")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Training extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resume_id", nullable = false)
    private Resume resume;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @Column(nullable = false, length = 100)
    private String courseName;

    @Column(nullable = false, length = 100)
    private String institutionName;

    @Column(columnDefinition = "TEXT")
    private String detailedContent;

    // 연관관계
    @OneToMany(mappedBy = "training", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TrainingTechStack> trainingTechStacks;

    // 생성자
    private Training(Resume resume, LocalDate startDate, LocalDate endDate,
                     String courseName, String institutionName, String detailedContent) {
        this.resume = resume;
        this.startDate = startDate;
        this.endDate = endDate;
        this.courseName = courseName;
        this.institutionName = institutionName;
        this.detailedContent = detailedContent;
    }

    // 정적 팩토리 메서드
    public static Training createTraining(Resume resume, LocalDate startDate, LocalDate endDate,
                                          String courseName, String institutionName,
                                          String detailedContent) {
        return new Training(resume, startDate, endDate, courseName, institutionName, detailedContent);
    }

    // 비즈니스 메서드
    public void updateTrainingPeriod(LocalDate startDate, LocalDate endDate) {
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public void updateTrainingInfo(String courseName, String institutionName) {
        this.courseName = courseName;
        this.institutionName = institutionName;
    }

    public void updateDetailedContent(String detailedContent) {
        this.detailedContent = detailedContent;
    }
}