package com.cvmento.domain.resume.entity;

import com.cvmento.global.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "custom_link")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CustomLink extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resume_id", nullable = false)
    private Resume resume;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false, length = 200)
    private String url;

    // 생성자
    private CustomLink(Resume resume, String name, String url) {
        this.resume = resume;
        this.name = name;
        this.url = url;
    }

    // 정적 팩토리 메서드
    public static CustomLink createCustomLink(Resume resume, String name, String url) {
        return new CustomLink(resume, name, url);
    }

    // 비즈니스 메서드
    public void updateLink(String name, String url) {
        this.name = name;
        this.url = url;
    }
}