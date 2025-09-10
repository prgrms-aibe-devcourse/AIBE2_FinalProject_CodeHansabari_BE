package com.cvmento.domain.resume.entity;

import com.cvmento.global.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "project_tech_stack")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProjectTechStack extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tech_stack_id", nullable = false)
    private TechStack techStack;

    @Column(length = 50)
    private String usageType;

    // 생성자
    private ProjectTechStack(Project project, TechStack techStack, String usageType) {
        this.project = project;
        this.techStack = techStack;
        this.usageType = usageType;
    }

    // 정적 팩토리 메서드
    public static ProjectTechStack createProjectTechStack(Project project, TechStack techStack,
                                                          String usageType) {
        return new ProjectTechStack(project, techStack, usageType);
    }

    // 비즈니스 메서드
    public void updateUsageType(String usageType) {
        this.usageType = usageType;
    }
}