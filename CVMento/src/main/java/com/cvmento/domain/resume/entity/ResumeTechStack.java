package com.cvmento.domain.resume.entity;

import com.cvmento.global.common.entity.BaseTimeEntity;
import com.cvmento.domain.resume.enums.ProficiencyLevel;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "resume_tech_stack")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ResumeTechStack extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resume_id", nullable = false)
    private Resume resume;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tech_stack_id", nullable = false)
    private TechStack techStack;

    @Enumerated(EnumType.STRING)
    private ProficiencyLevel proficiencyLevel;

    // 생성자
    private ResumeTechStack(Resume resume, TechStack techStack, ProficiencyLevel proficiencyLevel) {
        this.resume = resume;
        this.techStack = techStack;
        this.proficiencyLevel = proficiencyLevel;
    }

    // 정적 팩토리 메서드
    public static ResumeTechStack createResumeTechStack(Resume resume, TechStack techStack,
                                                        ProficiencyLevel proficiencyLevel) {
        return new ResumeTechStack(resume, techStack, proficiencyLevel);
    }

    // 비즈니스 메서드
    public void updateProficiencyLevel(ProficiencyLevel proficiencyLevel) {
        this.proficiencyLevel = proficiencyLevel;
    }
}