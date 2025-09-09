package com.cvmento.domain.coverLetter.entity;

import com.cvmento.domain.interview.entity.CoverLetterQna;
import com.cvmento.domain.member.entity.Member;
import com.cvmento.global.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Table(name = "cover_letter")
public class CoverLetter extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cover_letter_id")
    private Long coverLetterId;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "job_field", length = 100)
    private String jobField;  // 지원분야

    @Column(name = "experience_years")
    private Integer experienceYears;  // 경력 년수

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @OneToMany(mappedBy = "coverLetter", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CoverLetterQna> qnaList = new ArrayList<>();

    protected CoverLetter() {}

    // 기본 생성자 (기존 호환성 유지)
    public CoverLetter(String title, String content, Member member) {
        this.title = title;
        this.content = content;
        this.member = member;
    }

    // 확장 생성자 (경력 정보 포함)
    public CoverLetter(String title, String content, String jobField,
                       Integer experienceYears, Member member) {
        this.title = title;
        this.content = content;
        this.jobField = jobField;
        this.experienceYears = experienceYears;
        this.member = member;
    }
    // 총 경력을 문자열로 반환하는 헬퍼 메서드
    public String getTotalExperienceString() {
        if (experienceYears == null || experienceYears == 0) {
            return "신입";
        }
        return experienceYears + "년";
    }

    // 자소서 정보 업데이트 메서드
    public void updateCoverLetter(String title, String content, String jobField, Integer experienceYears) {
        this.title = title;
        this.content = content;
        this.jobField = jobField;
        this.experienceYears = experienceYears;
    }
}