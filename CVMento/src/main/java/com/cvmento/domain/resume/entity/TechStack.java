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

    private TechStack(String name, String category) {
        this.name = name;
        this.category = category;
    }

    public static TechStack createTechStack(String name, String category) {
        return new TechStack(name, category);
    }

}