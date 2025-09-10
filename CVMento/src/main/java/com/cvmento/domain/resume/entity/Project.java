package com.cvmento.domain.resume.entity;

import com.cvmento.global.common.entity.BaseTimeEntity;
import com.cvmento.domain.resume.enums.ProjectType;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "project")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Project extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resume_id", nullable = false)
    private Resume resume;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "career_id")
    private Career career;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "TEXT")
    private String detailedDescription;

    @Column(length = 200)
    private String repositoryUrl;

    @Column(length = 200)
    private String deployUrl;

    @Enumerated(EnumType.STRING)
    private ProjectType projectType;

    // 연관관계
    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProjectTechStack> projectTechStacks;

    // 생성자
    private Project(Resume resume, Career career, LocalDate startDate, LocalDate endDate,
                    String name, String description, String detailedDescription,
                    String repositoryUrl, String deployUrl, ProjectType projectType) {
        this.resume = resume;
        this.career = career;
        this.startDate = startDate;
        this.endDate = endDate;
        this.name = name;
        this.description = description;
        this.detailedDescription = detailedDescription;
        this.repositoryUrl = repositoryUrl;
        this.deployUrl = deployUrl;
        this.projectType = projectType;
    }

    // 정적 팩토리 메서드
    public static Project createProject(Resume resume, Career career, LocalDate startDate,
                                        LocalDate endDate, String name, String description,
                                        String detailedDescription, String repositoryUrl,
                                        String deployUrl, ProjectType projectType) {
        return new Project(resume, career, startDate, endDate, name, description,
                detailedDescription, repositoryUrl, deployUrl, projectType);
    }

    // 비즈니스 메서드
    public void updateProjectPeriod(LocalDate startDate, LocalDate endDate) {
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public void updateProjectInfo(String name, String description, String detailedDescription) {
        this.name = name;
        this.description = description;
        this.detailedDescription = detailedDescription;
    }

    public void updateProjectUrls(String repositoryUrl, String deployUrl) {
        this.repositoryUrl = repositoryUrl;
        this.deployUrl = deployUrl;
    }

    public void updateProjectType(ProjectType projectType) {
        this.projectType = projectType;
    }

    public void linkToCareer(Career career) {
        this.career = career;
    }
}