package com.cvmento.domain.resume.repository;

import com.cvmento.domain.member.entity.Member;
import com.cvmento.domain.resume.dto.request.*;
import com.cvmento.domain.resume.dto.response.*;
import com.cvmento.domain.resume.entity.*;
import com.cvmento.domain.resume.enums.ResumeStatus;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import static com.cvmento.domain.resume.entity.QAdditionalInfo.additionalInfo;
import static com.cvmento.domain.resume.entity.QCareer.career;
import static com.cvmento.domain.resume.entity.QCareerTechStack.careerTechStack;
import static com.cvmento.domain.resume.entity.QCustomLink.customLink;
import static com.cvmento.domain.resume.entity.QEducation.education;
import static com.cvmento.domain.resume.entity.QProject.project;
import static com.cvmento.domain.resume.entity.QProjectTechStack.projectTechStack;
import static com.cvmento.domain.resume.entity.QResumeTechStack.resumeTechStack;
import static com.cvmento.domain.resume.entity.QTechStack.techStack;
import static com.cvmento.domain.resume.entity.QTraining.training;
import static com.cvmento.domain.resume.entity.QTrainingTechStack.trainingTechStack;

import java.util.ArrayList;
import java.util.List;

/**
 * 이력서 관련 QueryDSL 구현체
 * - 이력서 상세 정보 저장/삭제/조회
 * - QueryDSL Projection을 활용한 DTO 직접 매핑
 */
@Repository
@RequiredArgsConstructor
@Slf4j
public class ResumeRepositoryImpl implements ResumeRepositoryCustom {

    private final JPAQueryFactory queryFactory;
    private final EntityManager entityManager;

    /**
     * 이력서 상세 정보 일괄 저장
     * 모든 하위 엔티티들을 순차적으로 저장
     */
    @Override
    public void saveResumeDetails(ResumeSaveRequest request, Resume resume) {
        log.debug("이력서 상세 정보 저장 시작 - Resume ID: {}", resume.getId());

        saveEducations(request.educations(), resume);
        saveResumeTechStacks(request.techStacks(), resume);
        saveCustomLinks(request.customLinks(), resume);
        saveCareers(request.careers(), resume);
        saveProjects(request.projects(), resume);
        saveTrainings(request.trainings(), resume);
        saveAdditionalInfos(request.additionalInfos(), resume);

        entityManager.flush();
        entityManager.clear();

        log.debug("이력서 상세 정보 저장 완료 - Resume ID: {}", resume.getId());
    }

    /**
     * 이력서 하위 데이터 전체 삭제
     * FK 제약조건을 고려하여 순서대로 삭제
     */
    @Override
    public void deleteAllResumeDetails(Resume resume) {
        Long resumeId = resume.getId();
        log.debug("이력서 상세 정보 삭제 시작 - Resume ID: {}", resumeId);

        // 1. 중간 테이블 삭제 (FK 제약조건 고려)
        deleteCareerTechStacks(resumeId);
        deleteProjectTechStacks(resumeId);
        deleteTrainingTechStacks(resumeId);

        // 2. 메인 테이블 삭제
        deleteEducations(resumeId);
        deleteResumeTechStacks(resumeId);
        deleteCustomLinks(resumeId);
        deleteCareers(resumeId);
        deleteProjects(resumeId);
        deleteTrainings(resumeId);
        deleteAdditionalInfos(resumeId);

        entityManager.flush();
        log.debug("이력서 상세 정보 삭제 완료 - Resume ID: {}", resumeId);
    }

    /**
     * 이력서 상세 정보를 DTO로 직접 조회
     * QueryDSL Projection을 활용하여 DB에서 바로 DTO 매핑
     */
    @Override
    public ResumeDetailResponse findResumeDetailByIdAndMember(Long resumeId, Member member, ResumeStatus status) {
        // 1. 기본 이력서 정보 조회
        Resume resume = queryFactory
                .selectFrom(QResume.resume)
                .where(
                        QResume.resume.id.eq(resumeId)
                                .and(QResume.resume.member.eq(member))
                                .and(QResume.resume.status.eq(status))
                )
                .fetchOne();

        if (resume == null) {
            return null;
        }

        // 2. 각 섹션별 상세 정보를 DTO로 조회하여 통합
        return new ResumeDetailResponse(
                resume.getId(),
                resume.getTitle(),
                resume.getType(),
                resume.getName(),
                resume.getEmail(),
                resume.getBirthYear(),
                resume.getPhone(),
                resume.getCareerType(),
                resume.getFieldName(),
                resume.getIntroduction(),
                resume.getGithubUrl(),
                resume.getBlogUrl(),
                resume.getNotionUrl(),
                resume.getCreatedAt(),
                resume.getUpdatedAt(),
                findEducationsByResumeId(resumeId),
                findResumeTechStacksByResumeId(resumeId),
                findCustomLinksByResumeId(resumeId),
                findCareersByResumeId(resumeId),
                findProjectsByResumeId(resumeId),
                findTrainingsByResumeId(resumeId),
                findAdditionalInfosByResumeId(resumeId)
        );
    }

    /**
     * 학력 정보 저장
     */
    private void saveEducations(List<EducationSaveRequest> requests, Resume resume) {
        if (requests == null || requests.isEmpty()) return;

        List<EducationSaveRequest> validRequests = requests.stream()
                .filter(this::isValidEducation)
                .toList();

        if (validRequests.isEmpty()) return;

        List<Education> educations = validRequests.stream()
                .map(req -> Education.createEducation(
                        resume, req.schoolName(), req.major(), req.degreeLevel(),
                        req.personalGpa(), req.totalGpa(), req.graduationDate()))
                .toList();

        educations.forEach(entityManager::persist);
        log.debug("학력 정보 {} 건 저장", educations.size());
    }

    /**
     * 이력서 기술스택 저장
     */
    private void saveResumeTechStacks(List<ResumeTechStackSaveRequest> requests, Resume resume) {
        if (requests == null || requests.isEmpty()) return;

        List<ResumeTechStackSaveRequest> validRequests = requests.stream()
                .filter(this::isValidResumeTechStack)
                .toList();

        if (validRequests.isEmpty()) return;

        List<ResumeTechStack> resumeTechStacks = validRequests.stream()
                .map(req -> {
                    TechStack techStack = findTechStackById(req.techStackId());
                    return ResumeTechStack.createResumeTechStack(resume, techStack, req.proficiencyLevel());
                })
                .toList();

        resumeTechStacks.forEach(entityManager::persist);
        log.debug("이력서 기술스택 {} 건 저장", resumeTechStacks.size());
    }

    /**
     * 커스텀 링크 저장
     */
    private void saveCustomLinks(List<CustomLinkSaveRequest> requests, Resume resume) {
        if (requests == null || requests.isEmpty()) return;

        List<CustomLinkSaveRequest> validRequests = requests.stream()
                .filter(this::isValidCustomLink)
                .toList();

        if (validRequests.isEmpty()) return;

        List<CustomLink> customLinks = validRequests.stream()
                .map(req -> CustomLink.createCustomLink(resume, req.name(), req.url()))
                .toList();

        customLinks.forEach(entityManager::persist);
        log.debug("커스텀 링크 {} 건 저장", customLinks.size());
    }

    /**
     * 경력 정보 저장 (기술스택 포함)
     */
    private void saveCareers(List<CareerSaveRequest> requests, Resume resume) {
        if (requests == null || requests.isEmpty()) return;

        List<CareerSaveRequest> validRequests = requests.stream()
                .filter(this::isValidCareer)
                .toList();

        if (validRequests.isEmpty()) return;

        for (CareerSaveRequest req : validRequests) {
            Career career = Career.createCareer(
                    resume, req.startDate(), req.endDate(), req.companyName(),
                    req.companyDescription(), req.departmentPosition(), req.mainTasks());

            entityManager.persist(career);
            entityManager.flush(); // ID 생성을 위한 flush

            saveCareerTechStacks(req.techStacks(), career);
        }
        log.debug("경력 정보 {} 건 저장", validRequests.size());
    }

    /**
     * 경력별 기술스택 저장
     */
    private void saveCareerTechStacks(List<CareerTechStackSaveRequest> requests, Career career) {
        if (requests == null || requests.isEmpty()) return;

        List<CareerTechStackSaveRequest> validRequests = requests.stream()
                .filter(this::isValidCareerTechStack)
                .toList();

        if (validRequests.isEmpty()) return;

        List<CareerTechStack> careerTechStacks = validRequests.stream()
                .map(req -> {
                    TechStack techStack = findTechStackById(req.techStackId());
                    return CareerTechStack.createCareerTechStack(career, techStack);
                })
                .toList();

        careerTechStacks.forEach(entityManager::persist);
    }

    /**
     * 프로젝트 정보 저장 (기술스택 포함)
     */
    private void saveProjects(List<ProjectSaveRequest> requests, Resume resume) {
        if (requests == null || requests.isEmpty()) return;

        List<ProjectSaveRequest> validRequests = requests.stream()
                .filter(this::isValidProject)
                .toList();

        if (validRequests.isEmpty()) return;

        for (ProjectSaveRequest req : validRequests) {
            Project project = Project.createProject(
                    resume, null, req.startDate(), req.endDate(), req.name(),
                    req.description(), req.detailedDescription(), req.repositoryUrl(),
                    req.deployUrl(), req.projectType());

            entityManager.persist(project);
            entityManager.flush(); // ID 생성을 위한 flush

            saveProjectTechStacks(req.techStacks(), project);
        }
        log.debug("프로젝트 정보 {} 건 저장", validRequests.size());
    }

    /**
     * 프로젝트별 기술스택 저장
     */
    private void saveProjectTechStacks(List<ProjectTechStackSaveRequest> requests, Project project) {
        if (requests == null || requests.isEmpty()) return;

        List<ProjectTechStackSaveRequest> validRequests = requests.stream()
                .filter(this::isValidProjectTechStack)
                .toList();

        if (validRequests.isEmpty()) return;

        List<ProjectTechStack> projectTechStacks = validRequests.stream()
                .map(req -> {
                    TechStack techStack = findTechStackById(req.techStackId());
                    return ProjectTechStack.createProjectTechStack(project, techStack, req.usageType());
                })
                .toList();

        projectTechStacks.forEach(entityManager::persist);
    }

    /**
     * 교육이력 저장 (기술스택 포함)
     */
    private void saveTrainings(List<TrainingSaveRequest> requests, Resume resume) {
        if (requests == null || requests.isEmpty()) return;

        List<TrainingSaveRequest> validRequests = requests.stream()
                .filter(this::isValidTraining)
                .toList();

        if (validRequests.isEmpty()) return;

        for (TrainingSaveRequest req : validRequests) {
            Training training = Training.createTraining(
                    resume, req.startDate(), req.endDate(), req.courseName(),
                    req.institutionName(), req.detailedContent());

            entityManager.persist(training);
            entityManager.flush(); // ID 생성을 위한 flush

            saveTrainingTechStacks(req.techStacks(), training);
        }
        log.debug("교육이력 {} 건 저장", validRequests.size());
    }

    /**
     * 교육이력별 기술스택 저장
     */
    private void saveTrainingTechStacks(List<TrainingTechStackSaveRequest> requests, Training training) {
        if (requests == null || requests.isEmpty()) return;

        List<TrainingTechStackSaveRequest> validRequests = requests.stream()
                .filter(this::isValidTrainingTechStack)
                .toList();

        if (validRequests.isEmpty()) return;

        List<TrainingTechStack> trainingTechStacks = validRequests.stream()
                .map(req -> {
                    TechStack techStack = findTechStackById(req.techStackId());
                    return TrainingTechStack.createTrainingTechStack(training, techStack);
                })
                .toList();

        trainingTechStacks.forEach(entityManager::persist);
    }

    /**
     * 기타사항 저장
     */
    private void saveAdditionalInfos(List<AdditionalInfoSaveRequest> requests, Resume resume) {
        if (requests == null || requests.isEmpty()) return;

        List<AdditionalInfoSaveRequest> validRequests = requests.stream()
                .filter(this::isValidAdditionalInfo)
                .toList();

        if (validRequests.isEmpty()) return;

        List<AdditionalInfo> additionalInfos = validRequests.stream()
                .map(req -> AdditionalInfo.createAdditionalInfo(
                        resume, req.startDate(), req.endDate(), req.category(),
                        req.activityName(), req.relatedOrganization(), req.detailedContent(),
                        req.certificateNumber(), req.languageLevel()))
                .toList();

        additionalInfos.forEach(entityManager::persist);
        log.debug("기타사항 {} 건 저장", additionalInfos.size());
    }

    private void deleteCareerTechStacks(Long resumeId) {
        queryFactory
                .delete(careerTechStack)
                .where(careerTechStack.career.resume.id.eq(resumeId))
                .execute();
    }

    private void deleteProjectTechStacks(Long resumeId) {
        queryFactory
                .delete(projectTechStack)
                .where(projectTechStack.project.resume.id.eq(resumeId))
                .execute();
    }

    private void deleteTrainingTechStacks(Long resumeId) {
        queryFactory
                .delete(trainingTechStack)
                .where(trainingTechStack.training.resume.id.eq(resumeId))
                .execute();
    }

    private void deleteEducations(Long resumeId) {
        queryFactory
                .delete(education)
                .where(education.resume.id.eq(resumeId))
                .execute();
    }

    private void deleteResumeTechStacks(Long resumeId) {
        queryFactory
                .delete(resumeTechStack)
                .where(resumeTechStack.resume.id.eq(resumeId))
                .execute();
    }

    private void deleteCustomLinks(Long resumeId) {
        queryFactory
                .delete(customLink)
                .where(customLink.resume.id.eq(resumeId))
                .execute();
    }

    private void deleteCareers(Long resumeId) {
        queryFactory
                .delete(career)
                .where(career.resume.id.eq(resumeId))
                .execute();
    }

    private void deleteProjects(Long resumeId) {
        queryFactory
                .delete(project)
                .where(project.resume.id.eq(resumeId))
                .execute();
    }

    private void deleteTrainings(Long resumeId) {
        queryFactory
                .delete(training)
                .where(training.resume.id.eq(resumeId))
                .execute();
    }

    private void deleteAdditionalInfos(Long resumeId) {
        queryFactory
                .delete(additionalInfo)
                .where(additionalInfo.resume.id.eq(resumeId))
                .execute();
    }

    /**
     * 학력 정보를 DTO로 조회
     */
    private List<EducationResponse> findEducationsByResumeId(Long resumeId) {
        return queryFactory
                .select(Projections.constructor(EducationResponse.class,
                        education.schoolName,
                        education.major,
                        education.degreeLevel.stringValue(),
                        education.personalGpa,
                        education.totalGpa,
                        education.graduationDate.stringValue()
                ))
                .from(education)
                .where(education.resume.id.eq(resumeId))
                .fetch();
    }

    /**
     * 이력서 기술스택을 DTO로 조회
     */
    private List<ResumeTechStackResponse> findResumeTechStacksByResumeId(Long resumeId) {
        return queryFactory
                .select(Projections.constructor(ResumeTechStackResponse.class,
                        techStack.id,
                        techStack.name,
                        techStack.category,
                        resumeTechStack.proficiencyLevel.stringValue()
                ))
                .from(resumeTechStack)
                .join(resumeTechStack.techStack, techStack)
                .where(resumeTechStack.resume.id.eq(resumeId))
                .fetch();
    }

    /**
     * 커스텀 링크를 DTO로 조회
     */
    private List<CustomLinkResponse> findCustomLinksByResumeId(Long resumeId) {
        return queryFactory
                .select(Projections.constructor(CustomLinkResponse.class,
                        customLink.name,
                        customLink.url
                ))
                .from(customLink)
                .where(customLink.resume.id.eq(resumeId))
                .fetch();
    }

    /**
     * 경력 정보를 DTO로 조회 (기술스택 포함)
     */
    private List<CareerResponse> findCareersByResumeId(Long resumeId) {
        // 1. 기본 경력 정보 조회
        List<CareerResponse> careers = queryFactory
                .select(Projections.constructor(CareerResponse.class,
                        career.startDate.stringValue(),
                        career.endDate.stringValue(),
                        career.companyName,
                        career.companyDescription,
                        career.departmentPosition,
                        career.mainTasks,
                        Expressions.constant(new ArrayList<CareerTechStackResponse>())
                ))
                .from(career)
                .where(career.resume.id.eq(resumeId))
                .orderBy(career.id.asc())
                .fetch();

        // 2. 각 경력별 기술스택 조회하여 설정
        for (int i = 0; i < careers.size(); i++) {
            CareerResponse careerResponse = careers.get(i);
            List<CareerTechStackResponse> techStacks = findCareerTechStacksByCareerIndex(resumeId, i);
            careers.set(i, new CareerResponse(
                    careerResponse.startDate(),
                    careerResponse.endDate(),
                    careerResponse.companyName(),
                    careerResponse.companyDescription(),
                    careerResponse.departmentPosition(),
                    careerResponse.mainTasks(),
                    techStacks
            ));
        }

        return careers;
    }

    /**
     * 특정 순서의 경력에 대한 기술스택 조회
     */
    private List<CareerTechStackResponse> findCareerTechStacksByCareerIndex(Long resumeId, int careerIndex) {
        // 경력 목록을 ID 순으로 조회하여 인덱스로 특정 경력 선택
        List<Career> careersForIndex = queryFactory
                .selectFrom(career)
                .where(career.resume.id.eq(resumeId))
                .orderBy(career.id.asc())
                .fetch();

        if (careerIndex >= careersForIndex.size()) {
            return new ArrayList<>();
        }

        Long careerId = careersForIndex.get(careerIndex).getId();

        return queryFactory
                .select(Projections.constructor(CareerTechStackResponse.class,
                        techStack.id,
                        techStack.name,
                        techStack.category
                ))
                .from(careerTechStack)
                .join(careerTechStack.techStack, techStack)
                .where(careerTechStack.career.id.eq(careerId))
                .fetch();
    }

    /**
     * 프로젝트 정보를 DTO로 조회 (기술스택 포함)
     */
    private List<ProjectResponse> findProjectsByResumeId(Long resumeId) {
        // 1. 기본 프로젝트 정보 조회
        List<ProjectResponse> projects = queryFactory
                .select(Projections.constructor(ProjectResponse.class,
                        project.career.id,
                        project.startDate.stringValue(),
                        project.endDate.stringValue(),
                        project.name,
                        project.description,
                        project.detailedDescription,
                        project.repositoryUrl,
                        project.deployUrl,
                        project.projectType.stringValue(),
                        Expressions.constant(new ArrayList<ProjectTechStackResponse>())
                ))
                .from(project)
                .where(project.resume.id.eq(resumeId))
                .orderBy(project.id.asc())
                .fetch();

        // 2. 각 프로젝트별 기술스택 조회하여 설정
        for (int i = 0; i < projects.size(); i++) {
            ProjectResponse projectResponse = projects.get(i);
            List<ProjectTechStackResponse> techStacks = findProjectTechStacksByProjectIndex(resumeId, i);
            projects.set(i, new ProjectResponse(
                    projectResponse.careerId(),
                    projectResponse.startDate(),
                    projectResponse.endDate(),
                    projectResponse.name(),
                    projectResponse.description(),
                    projectResponse.detailedDescription(),
                    projectResponse.repositoryUrl(),
                    projectResponse.deployUrl(),
                    projectResponse.projectType(),
                    techStacks
            ));
        }

        return projects;
    }

    /**
     * 특정 순서의 프로젝트에 대한 기술스택 조회
     */
    private List<ProjectTechStackResponse> findProjectTechStacksByProjectIndex(Long resumeId, int projectIndex) {
        List<Project> projectsForIndex = queryFactory
                .selectFrom(project)
                .where(project.resume.id.eq(resumeId))
                .orderBy(project.id.asc())
                .fetch();

        if (projectIndex >= projectsForIndex.size()) {
            return new ArrayList<>();
        }

        Long projectId = projectsForIndex.get(projectIndex).getId();

        return queryFactory
                .select(Projections.constructor(ProjectTechStackResponse.class,
                        techStack.id,
                        techStack.name,
                        techStack.category,
                        projectTechStack.usageType
                ))
                .from(projectTechStack)
                .join(projectTechStack.techStack, techStack)
                .where(projectTechStack.project.id.eq(projectId))
                .fetch();
    }

    /**
     * 교육이력을 DTO로 조회 (기술스택 포함)
     */
    private List<TrainingResponse> findTrainingsByResumeId(Long resumeId) {
        // 1. 기본 교육이력 정보 조회
        List<TrainingResponse> trainings = queryFactory
                .select(Projections.constructor(TrainingResponse.class,
                        training.startDate.stringValue(),
                        training.endDate.stringValue(),
                        training.courseName,
                        training.institutionName,
                        training.detailedContent,
                        Expressions.constant(new ArrayList<TrainingTechStackResponse>())
                ))
                .from(training)
                .where(training.resume.id.eq(resumeId))
                .orderBy(training.id.asc())
                .fetch();

        // 2. 각 교육이력별 기술스택 조회하여 설정
        for (int i = 0; i < trainings.size(); i++) {
            TrainingResponse trainingResponse = trainings.get(i);
            List<TrainingTechStackResponse> techStacks = findTrainingTechStacksByTrainingIndex(resumeId, i);
            trainings.set(i, new TrainingResponse(
                    trainingResponse.startDate(),
                    trainingResponse.endDate(),
                    trainingResponse.courseName(),
                    trainingResponse.institutionName(),
                    trainingResponse.detailedContent(),
                    techStacks
            ));
        }

        return trainings;
    }

    /**
     * 특정 순서의 교육이력에 대한 기술스택 조회
     */
    private List<TrainingTechStackResponse> findTrainingTechStacksByTrainingIndex(Long resumeId, int trainingIndex) {
        List<Training> trainingsForIndex = queryFactory
                .selectFrom(training)
                .where(training.resume.id.eq(resumeId))
                .orderBy(training.id.asc())
                .fetch();

        if (trainingIndex >= trainingsForIndex.size()) {
            return new ArrayList<>();
        }

        Long trainingId = trainingsForIndex.get(trainingIndex).getId();

        return queryFactory
                .select(Projections.constructor(TrainingTechStackResponse.class,
                        techStack.id,
                        techStack.name,
                        techStack.category
                ))
                .from(trainingTechStack)
                .join(trainingTechStack.techStack, techStack)
                .where(trainingTechStack.training.id.eq(trainingId))
                .fetch();
    }

    /**
     * 기타사항을 DTO로 조회
     */
    private List<AdditionalInfoResponse> findAdditionalInfosByResumeId(Long resumeId) {
        return queryFactory
                .select(Projections.constructor(AdditionalInfoResponse.class,
                        additionalInfo.startDate.stringValue(),
                        additionalInfo.endDate.stringValue(),
                        additionalInfo.category.stringValue(),
                        additionalInfo.activityName,
                        additionalInfo.relatedOrganization,
                        additionalInfo.detailedContent,
                        additionalInfo.certificateNumber,
                        additionalInfo.languageLevel
                ))
                .from(additionalInfo)
                .where(additionalInfo.resume.id.eq(resumeId))
                .fetch();
    }

    /**
     * 기술스택 ID로 엔티티 조회
     */
    private TechStack findTechStackById(Long techStackId) {
        return entityManager.find(TechStack.class, techStackId);
    }

    private boolean isValidEducation(EducationSaveRequest request) {
        return request != null &&
                request.schoolName() != null && !request.schoolName().trim().isEmpty() &&
                request.degreeLevel() != null &&
                request.graduationDate() != null;
    }

    private boolean isValidResumeTechStack(ResumeTechStackSaveRequest request) {
        return request != null && request.techStackId() != null;
    }

    private boolean isValidCustomLink(CustomLinkSaveRequest request) {
        return request != null &&
                request.name() != null && !request.name().trim().isEmpty() &&
                request.url() != null && !request.url().trim().isEmpty();
    }

    private boolean isValidCareer(CareerSaveRequest request) {
        return request != null &&
                request.startDate() != null &&
                request.endDate() != null &&
                request.companyName() != null && !request.companyName().trim().isEmpty() &&
                request.departmentPosition() != null && !request.departmentPosition().trim().isEmpty();
    }

    private boolean isValidCareerTechStack(CareerTechStackSaveRequest request) {
        return request != null && request.techStackId() != null;
    }

    private boolean isValidProject(ProjectSaveRequest request) {
        return request != null &&
                request.startDate() != null &&
                request.endDate() != null &&
                request.name() != null && !request.name().trim().isEmpty();
    }

    private boolean isValidProjectTechStack(ProjectTechStackSaveRequest request) {
        return request != null && request.techStackId() != null;
    }

    private boolean isValidTraining(TrainingSaveRequest request) {
        return request != null &&
                request.startDate() != null &&
                request.endDate() != null &&
                request.courseName() != null && !request.courseName().trim().isEmpty() &&
                request.institutionName() != null && !request.institutionName().trim().isEmpty();
    }

    private boolean isValidTrainingTechStack(TrainingTechStackSaveRequest request) {
        return request != null && request.techStackId() != null;
    }

    private boolean isValidAdditionalInfo(AdditionalInfoSaveRequest request) {
        return request != null &&
                request.startDate() != null &&
                request.category() != null &&
                request.activityName() != null && !request.activityName().trim().isEmpty() &&
                request.relatedOrganization() != null && !request.relatedOrganization().trim().isEmpty();
    }
}