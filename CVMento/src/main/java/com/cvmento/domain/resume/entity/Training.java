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

    @OneToMany(mappedBy = "training", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TrainingTechStack> trainingTechStacks;

    private Training(Resume resume, LocalDate startDate, LocalDate endDate,
                     String courseName, String institutionName, String detailedContent) {
        this.resume = resume;
        this.startDate = startDate;
        this.endDate = endDate;
        this.courseName = courseName;
        this.institutionName = institutionName;
        this.detailedContent = detailedContent;
    }

    public static Training createTraining(Resume resume, LocalDate startDate, LocalDate endDate,
                                          String courseName, String institutionName,
                                          String detailedContent) {
        return new Training(resume, startDate, endDate, courseName, institutionName, detailedContent);
    }

}