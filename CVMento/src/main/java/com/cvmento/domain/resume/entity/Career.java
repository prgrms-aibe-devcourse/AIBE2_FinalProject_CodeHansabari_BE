package com.cvmento.domain.resume.entity;

import com.cvmento.global.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "career")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Career extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resume_id", nullable = false)
    private Resume resume;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @Column(nullable = false, length = 100)
    private String companyName;

    @Column(columnDefinition = "TEXT")
    private String companyDescription;

    @Column(nullable = false, length = 100)
    private String departmentPosition;

    @Column(columnDefinition = "TEXT")
    private String mainTasks;

    @OneToMany(mappedBy = "career", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CareerTechStack> careerTechStacks;

    @OneToMany(mappedBy = "career", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Project> projects;

    /** 생성자 */
    private Career(Resume resume, LocalDate startDate, LocalDate endDate, String companyName,
                   String companyDescription, String departmentPosition, String mainTasks) {
        this.resume = resume;
        this.startDate = startDate;
        this.endDate = endDate;
        this.companyName = companyName;
        this.companyDescription = companyDescription;
        this.departmentPosition = departmentPosition;
        this.mainTasks = mainTasks;
    }

    /** 생성 메서드 */
    public static Career createCareer(Resume resume, LocalDate startDate, LocalDate endDate,
                                      String companyName, String companyDescription,
                                      String departmentPosition, String mainTasks) {
        return new Career(resume, startDate, endDate, companyName, companyDescription,
                departmentPosition, mainTasks);
    }
}
