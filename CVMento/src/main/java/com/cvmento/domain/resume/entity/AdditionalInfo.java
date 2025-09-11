package com.cvmento.domain.resume.entity;

import com.cvmento.global.common.entity.BaseTimeEntity;
import com.cvmento.domain.resume.enums.AdditionalInfoCategory;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "additional_info")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AdditionalInfo extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resume_id", nullable = false)
    private Resume resume;

    @Column(nullable = false)
    private LocalDate startDate;

    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AdditionalInfoCategory category;

    @Column(nullable = false, length = 100)
    private String activityName;

    @Column(nullable = false, length = 100)
    private String relatedOrganization;

    @Column(columnDefinition = "TEXT")
    private String detailedContent;

    @Column(length = 50)
    private String certificateNumber;

    @Column(length = 50)
    private String languageLevel;

    // 생성자
    private AdditionalInfo(Resume resume, LocalDate startDate, LocalDate endDate,
                           AdditionalInfoCategory category, String activityName,
                           String relatedOrganization, String detailedContent,
                           String certificateNumber, String languageLevel) {
        this.resume = resume;
        this.startDate = startDate;
        this.endDate = endDate;
        this.category = category;
        this.activityName = activityName;
        this.relatedOrganization = relatedOrganization;
        this.detailedContent = detailedContent;
        this.certificateNumber = certificateNumber;
        this.languageLevel = languageLevel;
    }

    // 정적 팩토리 메서드
    public static AdditionalInfo createAdditionalInfo(Resume resume, LocalDate startDate,
                                                      LocalDate endDate, AdditionalInfoCategory category,
                                                      String activityName, String relatedOrganization,
                                                      String detailedContent, String certificateNumber,
                                                      String languageLevel) {
        return new AdditionalInfo(resume, startDate, endDate, category, activityName,
                relatedOrganization, detailedContent, certificateNumber, languageLevel);
    }

    // 비즈니스 메서드
    public void updatePeriod(LocalDate startDate, LocalDate endDate) {
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public void updateBasicInfo(AdditionalInfoCategory category, String activityName,
                                String relatedOrganization) {
        this.category = category;
        this.activityName = activityName;
        this.relatedOrganization = relatedOrganization;
    }

    public void updateDetailedContent(String detailedContent) {
        this.detailedContent = detailedContent;
    }

    public void updateCertificateInfo(String certificateNumber) {
        this.certificateNumber = certificateNumber;
    }

    public void updateLanguageLevel(String languageLevel) {
        this.languageLevel = languageLevel;
    }
}