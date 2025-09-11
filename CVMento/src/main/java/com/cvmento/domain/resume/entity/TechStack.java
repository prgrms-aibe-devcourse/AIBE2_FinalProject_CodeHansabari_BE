package com.cvmento.domain.resume.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tech_stack")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TechStack {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(length = 50)
    private String category;

    // 생성자
    private TechStack(String name, String category) {
        this.name = name;
        this.category = category;
    }

    // 정적 팩토리 메서드 (관리자용)
    public static TechStack createTechStack(String name, String category) {
        return new TechStack(name, category);
    }

    // 비즈니스 메서드
    public void updateTechStack(String name, String category) {
        this.name = name;
        this.category = category;
    }
}