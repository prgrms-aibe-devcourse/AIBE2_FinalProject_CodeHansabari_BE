package com.cvmento.domain.resume.repository;

import com.cvmento.domain.resume.dto.request.*;
import com.cvmento.domain.resume.entity.*;
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
import static com.cvmento.domain.resume.entity.QResume.resume;
import static com.cvmento.domain.resume.entity.QResumeTechStack.resumeTechStack;
import static com.cvmento.domain.resume.entity.QTraining.training;
import static com.cvmento.domain.resume.entity.QTrainingTechStack.trainingTechStack;

import java.util.List;

@Repository
@RequiredArgsConstructor
@Slf4j
public class ResumeRepositoryImpl implements ResumeRepositoryCustom {

    private final JPAQueryFactory queryFactory;
    private final EntityManager entityManager;

    @Override
    public void saveResumeDetails(ResumeSaveRequest request, Resume resume) {
        // 각 영역별로 배치 저장
        saveEducations(request.educations(), resume);
        saveResumeTechStacks(request.techStacks(), resume);
        saveCustomLinks(request.customLinks(), resume);
        saveCareers(request.careers(), resume);
        saveProjects(request.projects(), resume);
        saveTrainings(request.trainings(), resume);
        saveAdditionalInfos(request.additionalInfos(), resume);

        // 변경사항 flush
        entityManager.flush();
        entityManager.clear();

        log.debug("이력서 상세 정보 저장 완료 - Resume ID: {}", resume.getId());
    }

    @Override
    public void deleteAllResumeDetails(Resume resume) {
        Long resumeId = resume.getId();

        // 1. 중간 테이블부터 삭제 (FK 제약조건 고려)
        deleteCareerTechStacks(resumeId);
        deleteProjectTechStacks(resumeId);
        deleteTrainingTechStacks(resumeId);

        // 2. 자식 테이블들 삭제
        deleteEducations(resumeId);
        deleteResumeTechStacks(resumeId);
        deleteCustomLinks(resumeId);
        deleteCareers(resumeId);
        deleteProjects(resumeId);
        deleteTrainings(resumeId);
        deleteAdditionalInfos(resumeId);

        entityManager.flush();
        log.debug("이력서 ID: {}의 모든 상세 정보 삭제 완료", resumeId);
    }

    // ======================== 개별 저장 메서드들 ========================

    private void saveEducations(List<EducationSaveRequest> requests, Resume resume) {
        if (requests == null || requests.isEmpty()) return;

        List<Education> educations = requests.stream()
                .map(req -> Education.createEducation(
                        resume, req.schoolName(), req.major(), req.degreeLevel(),
                        req.personalGpa(), req.totalGpa(), req.graduationDate()))
                .toList();

        educations.forEach(entityManager::persist);
        log.debug("학력 정보 {} 건 저장", educations.size());
    }

    private void saveResumeTechStacks(List<ResumeTechStackSaveRequest> requests, Resume resume) {
        if (requests == null || requests.isEmpty()) return;

        List<ResumeTechStack> resumeTechStacks = requests.stream()
                .map(req -> {
                    TechStack techStack = findTechStackById(req.techStackId());
                    return ResumeTechStack.createResumeTechStack(resume, techStack, req.proficiencyLevel());
                })
                .toList();

        resumeTechStacks.forEach(entityManager::persist);
        log.debug("이력서 기술스택 {} 건 저장", resumeTechStacks.size());
    }

    private void saveCustomLinks(List<CustomLinkSaveRequest> requests, Resume resume) {
        if (requests == null || requests.isEmpty()) return;

        List<CustomLink> customLinks = requests.stream()
                .map(req -> CustomLink.createCustomLink(resume, req.name(), req.url()))
                .toList();

        customLinks.forEach(entityManager::persist);
        log.debug("커스텀 링크 {} 건 저장", customLinks.size());
    }

    private void saveCareers(List<CareerSaveRequest> requests, Resume resume) {
        if (requests == null || requests.isEmpty()) return;

        for (CareerSaveRequest req : requests) {
            Career career = Career.createCareer(
                    resume, req.startDate(), req.endDate(), req.companyName(),
                    req.companyDescription(), req.departmentPosition(), req.mainTasks());

            entityManager.persist(career);
            entityManager.flush(); // Career ID 생성을 위해

            // 경력 기술스택 저장
            saveCareerTechStacks(req.techStacks(), career);
        }
        log.debug("경력 정보 {} 건 저장", requests.size());
    }

    private void saveCareerTechStacks(List<CareerTechStackSaveRequest> requests, Career career) {
        if (requests == null || requests.isEmpty()) return;

        List<CareerTechStack> careerTechStacks = requests.stream()
                .map(req -> {
                    TechStack techStack = findTechStackById(req.techStackId());
                    return CareerTechStack.createCareerTechStack(career, techStack);
                })
                .toList();

        careerTechStacks.forEach(entityManager::persist);
    }

    private void saveProjects(List<ProjectSaveRequest> requests, Resume resume) {
        if (requests == null || requests.isEmpty()) return;

        for (ProjectSaveRequest req : requests) {
            Project project = Project.createProject(
                    resume, null, req.startDate(), req.endDate(), req.name(),
                    req.description(), req.detailedDescription(), req.repositoryUrl(),
                    req.deployUrl(), req.projectType());

            entityManager.persist(project);
            entityManager.flush(); // Project ID 생성을 위해

            // 프로젝트 기술스택 저장
            saveProjectTechStacks(req.techStacks(), project);
        }
        log.debug("프로젝트 정보 {} 건 저장", requests.size());
    }

    private void saveProjectTechStacks(List<ProjectTechStackSaveRequest> requests, Project project) {
        if (requests == null || requests.isEmpty()) return;

        List<ProjectTechStack> projectTechStacks = requests.stream()
                .map(req -> {
                    TechStack techStack = findTechStackById(req.techStackId());
                    return ProjectTechStack.createProjectTechStack(project, techStack, req.usageType());
                })
                .toList();

        projectTechStacks.forEach(entityManager::persist);
    }

    private void saveTrainings(List<TrainingSaveRequest> requests, Resume resume) {
        if (requests == null || requests.isEmpty()) return;

        for (TrainingSaveRequest req : requests) {
            Training training = Training.createTraining(
                    resume, req.startDate(), req.endDate(), req.courseName(),
                    req.institutionName(), req.detailedContent());

            entityManager.persist(training);
            entityManager.flush(); // Training ID 생성을 위해

            // 교육 기술스택 저장
            saveTrainingTechStacks(req.techStacks(), training);
        }
        log.debug("교육이력 {} 건 저장", requests.size());
    }

    private void saveTrainingTechStacks(List<TrainingTechStackSaveRequest> requests, Training training) {
        if (requests == null || requests.isEmpty()) return;

        List<TrainingTechStack> trainingTechStacks = requests.stream()
                .map(req -> {
                    TechStack techStack = findTechStackById(req.techStackId());
                    return TrainingTechStack.createTrainingTechStack(training, techStack);
                })
                .toList();

        trainingTechStacks.forEach(entityManager::persist);
    }

    private void saveAdditionalInfos(List<AdditionalInfoSaveRequest> requests, Resume resume) {
        if (requests == null || requests.isEmpty()) return;

        List<AdditionalInfo> additionalInfos = requests.stream()
                .map(req -> AdditionalInfo.createAdditionalInfo(
                        resume, req.startDate(), req.endDate(), req.category(),
                        req.activityName(), req.relatedOrganization(), req.detailedContent(),
                        req.certificateNumber(), req.languageLevel()))
                .toList();

        additionalInfos.forEach(entityManager::persist);
        log.debug("기타사항 {} 건 저장", additionalInfos.size());
    }

    // ======================== 유틸리티 메서드 ========================

    private TechStack findTechStackById(Long techStackId) {
        return entityManager.find(TechStack.class, techStackId);
    }

    // ======================== 개별 삭제 메서드들 ========================

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
}