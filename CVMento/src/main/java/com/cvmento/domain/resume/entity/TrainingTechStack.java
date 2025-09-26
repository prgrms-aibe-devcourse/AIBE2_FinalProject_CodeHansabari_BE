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

    private TrainingTechStack(Training training, TechStack techStack) {
        this.training = training;
        this.techStack = techStack;
    }

    public static TrainingTechStack createTrainingTechStack(Training training, TechStack techStack) {
        return new TrainingTechStack(training, techStack);
    }
}