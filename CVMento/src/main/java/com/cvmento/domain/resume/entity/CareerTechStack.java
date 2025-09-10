package com.cvmento.domain.resume.entity;

import com.cvmento.global.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "career_tech_stack")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CareerTechStack extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "career_id", nullable = false)
    private Career career;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tech_stack_id", nullable = false)
    private TechStack techStack;

    // 생성자
    private CareerTechStack(Career career, TechStack techStack) {
        this.career = career;
        this.techStack = techStack;
    }

    // 정적 팩토리 메서드
    public static CareerTechStack createCareerTechStack(Career career, TechStack techStack) {
        return new CareerTechStack(career, techStack);
    }
}