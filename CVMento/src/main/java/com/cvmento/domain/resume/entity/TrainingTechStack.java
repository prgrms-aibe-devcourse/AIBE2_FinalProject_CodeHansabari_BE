package com.cvmento.domain.resume.entity;

import com.cvmento.global.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "training_tech_stack")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TrainingTechStack extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "training_id", nullable = false)
    private Training training;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tech_stack_id", nullable = false)
    private TechStack techStack;

    // 생성자
    private TrainingTechStack(Training training, TechStack techStack) {
        this.training = training;
        this.techStack = techStack;
    }

    // 정적 팩토리 메서드
    public static TrainingTechStack createTrainingTechStack(Training training, TechStack techStack) {
        return new TrainingTechStack(training, techStack);
    }
}