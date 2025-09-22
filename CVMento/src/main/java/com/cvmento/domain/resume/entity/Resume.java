package com.cvmento.domain.resume.entity;

import com.cvmento.global.common.entity.BaseTimeEntity;
import com.cvmento.domain.resume.enums.ResumeType;
import com.cvmento.domain.resume.enums.ResumeStatus;
import com.cvmento.domain.resume.enums.CareerType;
import com.cvmento.domain.member.entity.Member;
import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Entity
@Table(name = "resume", indexes = {
    @Index(name = "idx_resume_member_id", columnList = "member_id"),
    @Index(name = "idx_resume_status", columnList = "status"),
    @Index(name = "idx_resume_member_status", columnList = "member_id, status"),
    @Index(name = "idx_resume_status_updated", columnList = "status, updated_at")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Resume extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ResumeType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ResumeStatus status;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false, length = 100)
    private String email;

    @Column(nullable = false)
    private Integer birthYear;

    @Column(nullable = false, length = 20)
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CareerType careerType;

    @Column(nullable = false, length = 100)
    private String fieldName;

    @Column(columnDefinition = "TEXT")
    private String introduction;

    @Column(length = 200)
    private String githubUrl;

    @Column(length = 200)
    private String blogUrl;

    @Column(length = 200)
    private String notionUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    // 연관관계 매핑
    @OneToMany(mappedBy = "resume", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ResumeTechStack> resumeTechStacks;

    @OneToMany(mappedBy = "resume", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Education> educations;

    @OneToMany(mappedBy = "resume", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CustomLink> customLinks;

    @OneToMany(mappedBy = "resume", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Career> careers;

    @OneToMany(mappedBy = "resume", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Project> projects;

    @OneToMany(mappedBy = "resume", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Training> trainings;

    @OneToMany(mappedBy = "resume", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AdditionalInfo> additionalInfos;

    // 생성자 - 필수 필드만
    private Resume(String title, ResumeType type, String name, String email,
                   Integer birthYear, String phone, CareerType careerType,
                   String fieldName, Member member) {
        this.title = title;
        this.type = type;
        this.name = name;
        this.email = email;
        this.birthYear = birthYear;
        this.phone = phone;
        this.careerType = careerType;
        this.fieldName = fieldName;
        this.member = member;
        this.status = ResumeStatus.ACTIVE; // 기본값: 활성
    }

    // 정적 팩토리 메서드
    public static Resume createResume(String title, ResumeType type, String name,
                                      String email, Integer birthYear,
                                      String phone, CareerType careerType,
                                      String fieldName, Member member) {
        return new Resume(title, type, name, email, birthYear, phone, careerType, fieldName, member);
    }

    // 비즈니스 메서드
    public void updateTitle(String title) {
        this.title = title;
    }

    public void updateBasicInfo(String name, String email, Integer birthYear, String phone) {
        this.name = name;
        this.email = email;
        this.birthYear = birthYear;
        this.phone = phone;
    }

    public void updateType(ResumeType type) {
        this.type = type;
    }

    public void updateStatus(ResumeStatus status) {
        this.status = status;
    }

    public void updateFieldAndCareerType(String fieldName, CareerType careerType) {
        this.fieldName = fieldName;
        this.careerType = careerType;
    }

    public void updateIntroduction(String introduction) {
        this.introduction = introduction;
    }

    public void updateUrls(String githubUrl, String blogUrl, String notionUrl) {
        this.githubUrl = githubUrl;
        this.blogUrl = blogUrl;
        this.notionUrl = notionUrl;
    }

    public void restore() {
        if (this.status != ResumeStatus.DELETED) {
            throw new IllegalStateException("삭제된 상태의 이력서만 복구할 수 있습니다.");
        }
        this.status = ResumeStatus.ACTIVE;
    }

    public boolean isDeleted() {
        return this.status == ResumeStatus.DELETED;
    }
}