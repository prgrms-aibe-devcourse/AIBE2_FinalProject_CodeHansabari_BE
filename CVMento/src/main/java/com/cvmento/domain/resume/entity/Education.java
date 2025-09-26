package com.cvmento.domain.resume.entity;

import com.cvmento.global.common.entity.BaseTimeEntity;
import com.cvmento.domain.resume.enums.DegreeLevel;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "education")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Education extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resume_id", nullable = false)
    private Resume resume;

    @Column(nullable = false, length = 100)
    private String schoolName;

    @Column(length = 100)
    private String major;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DegreeLevel degreeLevel;

    @Column(precision = 3, scale = 2)
    private BigDecimal personalGpa;

    @Column(precision = 3, scale = 2)
    private BigDecimal totalGpa;

    @Column(nullable = false)
    private LocalDate graduationDate;

    private Education(Resume resume, String schoolName, String major, DegreeLevel degreeLevel,
                      BigDecimal personalGpa, BigDecimal totalGpa, LocalDate graduationDate) {
        this.resume = resume;
        this.schoolName = schoolName;
        this.major = major;
        this.degreeLevel = degreeLevel;
        this.personalGpa = personalGpa;
        this.totalGpa = totalGpa;
        this.graduationDate = graduationDate;
    }

    public static Education createEducation(Resume resume, String schoolName, String major,
                                            DegreeLevel degreeLevel, BigDecimal personalGpa, BigDecimal totalGpa, LocalDate graduationDate) {
        return new Education(resume, schoolName, major, degreeLevel, personalGpa, totalGpa, graduationDate);
    }
}