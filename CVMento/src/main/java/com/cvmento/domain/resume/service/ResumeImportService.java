package com.cvmento.domain.resume.service;

import com.cvmento.domain.resume.dto.VisionPromptResult;
import com.cvmento.domain.resume.dto.request.CareerSaveRequest;
import com.cvmento.domain.resume.dto.request.CareerTechStackSaveRequest;
import com.cvmento.domain.resume.dto.request.ProjectSaveRequest;
import com.cvmento.domain.resume.dto.request.ProjectTechStackSaveRequest;
import com.cvmento.domain.resume.dto.request.ResumeTechStackSaveRequest;
import com.cvmento.domain.resume.dto.request.TrainingSaveRequest;
import com.cvmento.domain.resume.dto.request.TrainingTechStackSaveRequest;
import com.cvmento.domain.resume.dto.response.ResumeImportResponse;
import com.cvmento.domain.resume.service.ResumeService;
import com.cvmento.global.aws.LambdaService;
import com.cvmento.global.exception.customException.FileSizeExceededException;
import com.cvmento.global.exception.customException.InvalidFileException;
import com.cvmento.global.exception.customException.UnsupportedFileTypeException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ResumeImportService {

    // 파일 검증 상수들
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
    private static final Set<String> ALLOWED_MIME_TYPES = Set.of(
            "application/pdf",
            "image/jpeg",
            "image/jpg",
            "image/png"
    );
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
            "pdf", "jpg", "jpeg", "png"
    );

    private final ResumeLlmPromptService resumeLlmPromptService;
    private final ResumeLlmClientService resumeLlmClientService;
    private final LambdaService lambdaService;
    private final ResumeService resumeService;
    private final TechStackMappingService techStackMappingService;

    @Value("${resume.import.strategy:direct}")
    private String importStrategy;

    public ResumeImportResponse importResume(MultipartFile file, String memberEmail) {
        MDC.put("spanId", "resume-import-service");

        validateFile(file);

        log.info("이력서 변환 시작 - 전략: {}, 파일명: {}, 크기: {}bytes",
                importStrategy, file.getOriginalFilename(), file.getSize());

        ResumeImportResponse response;
        if ("lambda".equals(importStrategy)) {
            response = importWithLambda(file);
        } else {
            response = importWithDirect(file);
        }

        // 변환 성공 시 자동 저장
        try {
            ResumeImportResponse mappedResponse = mapTechStackIdsToRealIds(response);
            saveConvertedResume(mappedResponse, memberEmail);
            log.info("이력서 변환 및 저장 모두 완료 - 제목: {}", response.title());
        } catch (Exception saveException) {
            log.error("이력서 저장 실패, 하지만 변환 결과는 반환 - 오류: {}", saveException.getMessage());
            // 저장 실패해도 변환 결과는 반환
        }

        return response;
    }

    private void validateFile(MultipartFile file) {
        // 1. 파일 존재 여부 확인
        if (file == null || file.isEmpty()) {
            throw new InvalidFileException("파일이 비어있습니다.");
        }

        // 2. MIME 타입 확인
        String contentType = file.getContentType();
        if (contentType == null) {
            throw new InvalidFileException("파일 타입을 확인할 수 없습니다.");
        }

        // 3. 허용된 MIME 타입인지 정확히 확인
        if (!ALLOWED_MIME_TYPES.contains(contentType.toLowerCase())) {
            throw new UnsupportedFileTypeException(
                    String.format("지원하지 않는 파일 형식입니다. 허용된 타입: %s, 요청된 타입: %s",
                            ALLOWED_MIME_TYPES, contentType));
        }

        // 4. 파일 확장자 추가 검증
        String fileName = file.getOriginalFilename();
        if (fileName != null && fileName.contains(".")) {
            String extension = getFileExtension(fileName).toLowerCase();
            if (!ALLOWED_EXTENSIONS.contains(extension)) {
                throw new UnsupportedFileTypeException(
                        String.format("지원하지 않는 파일 확장자입니다. 허용된 확장자: %s, 요청된 확장자: %s",
                                ALLOWED_EXTENSIONS, extension));
            }
        }

        // 5. 파일 크기 제한 확인
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new FileSizeExceededException(
                    String.format("파일 크기가 제한을 초과했습니다. 최대 크기: %dMB, 요청된 크기: %.2fMB",
                            MAX_FILE_SIZE / (1024 * 1024), file.getSize() / (1024.0 * 1024.0)));
        }

        log.info("파일 검증 완료 - 타입: {}, 확장자: {}, 크기: {}bytes",
                contentType, fileName != null ? getFileExtension(fileName) : "unknown", file.getSize());
    }

    private String getFileExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf(".") + 1);
    }

    private ResumeImportResponse importWithDirect(MultipartFile file) {
        MDC.put("spanId", "resume-import-direct");

        log.info("Direct 전략으로 이력서 변환 시작");

        // 모든 파일 타입에 대해 동일한 처리 (단순화)
        return processFile(file);
    }

    private ResumeImportResponse processFile(MultipartFile file) {
        // 1. Vision 프롬프트 생성 (Base64 이미지 포함)
        VisionPromptResult visionPrompt = resumeLlmPromptService.createVisionPrompt(file);

        // 2. Vision API 호출
        MDC.put("spanId", "resume-llm-client");
        ResumeImportResponse response = resumeLlmClientService.convertResumeWithVision(
                visionPrompt.textPrompt(), 
                visionPrompt.base64Image()
        );

        MDC.put("spanId", "resume-import-direct");
        log.info("Vision API 파일 변환 완료 - 이름: {}, 제목: {}", 
                response.name(), response.title());

        return response;
    }

    private ResumeImportResponse importWithLambda(MultipartFile file) {
        MDC.put("spanId", "resume-import-lambda");

        log.info("Lambda 전략으로 이력서 변환 시작");

        try {
            // 1. Lambda OCR로 텍스트 추출
            MDC.put("spanId", "lambda-ocr-service");
            String extractedText = lambdaService.invokeLambdaOcr(file);

            MDC.put("spanId", "resume-import-lambda");
            log.info("Lambda OCR 완료 - 추출된 텍스트 길이: {}chars", extractedText.length());

            // 2. 추출된 텍스트로 프롬프트 생성
            MDC.put("spanId", "resume-prompt-service");
            String prompt = resumeLlmPromptService.createResumeConversionPrompt(extractedText);

            // 3. LLM 호출
            MDC.put("spanId", "resume-llm-client");
            ResumeImportResponse response = resumeLlmClientService.convertResume(prompt);

            MDC.put("spanId", "resume-import-lambda");
            log.info("Lambda 전략 변환 완료 - 이름: {}, 제목: {}",
                    response.name(), response.title());

            return response;

        } catch (Exception e) {
            log.error("Lambda 전략 변환 실패, Direct 전략으로 대체: {}", e.getMessage());
            // Lambda 실패 시 Direct 전략으로 fallback
            return importWithDirect(file);
        }
    }

    @Transactional
    private void saveConvertedResume(ResumeImportResponse response, String memberEmail) {
        MDC.put("spanId", "resume-save-after-import");

        // ResumeImportResponse를 ResumeSaveRequest로 변환
        var saveRequest = response.toResumeSaveRequest();

        // 기존 ResumeService의 saveResume 메서드 사용
        resumeService.saveResume(saveRequest, memberEmail);

        log.info("변환된 이력서 저장 완료 - 제목: {}, 이름: {}",
                response.title(), response.name());
    }

    /**
     * 기술스택 이름을 실제 ID로 매핑
     */
    private ResumeImportResponse mapTechStackIdsToRealIds(ResumeImportResponse response) {
        MDC.put("spanId", "resume-techstack-mapping");

        try {
            log.info("기술스택 ID 매핑 시작 - 메인: {}개", response.techStacks().size());

            // 메인 기술스택 매핑
            var mappedTechStacks = response.techStacks().stream()
                    .map(this::mapResumeTechStack)
                    .toList();

            // Career 기술스택 매핑
            var mappedCareers = response.careers().stream()
                    .map(career -> new CareerSaveRequest(
                            career.startDate(), career.endDate(), career.companyName(),
                            career.companyDescription(), career.departmentPosition(), career.mainTasks(),
                            career.techStacks().stream().map(this::mapCareerTechStack).toList()
                    ))
                    .toList();

            // Project 기술스택 매핑
            var mappedProjects = response.projects().stream()
                    .map(project -> new ProjectSaveRequest(
                            project.startDate(), project.endDate(), project.name(),
                            project.description(), project.detailedDescription(),
                            project.repositoryUrl(), project.deployUrl(), project.projectType(),
                            project.techStacks().stream().map(this::mapProjectTechStack).toList()
                    ))
                    .toList();

            // Training 기술스택 매핑
            var mappedTrainings = response.trainings().stream()
                    .map(training -> new TrainingSaveRequest(
                            training.startDate(), training.endDate(),
                            training.courseName(),
                            training.institutionName(),
                            training.detailedContent(),
                            training.techStacks().stream().map(this::mapTrainingTechStack).toList()
                    ))
                    .toList();

            log.info("기술스택 ID 매핑 완료 - 메인: {}개, 경력: {}개, 프로젝트: {}개, 교육: {}개",
                    mappedTechStacks.size(), mappedCareers.size(), mappedProjects.size(), mappedTrainings.size());

            return new ResumeImportResponse(
                    response.title(), response.type(), response.name(), response.email(),
                    response.birthYear(), response.phone(), response.careerType(), response.fieldName(),
                    response.introduction(), response.githubUrl(), response.blogUrl(), response.notionUrl(),
                    response.educations(), mappedTechStacks, response.customLinks(),
                    mappedCareers, mappedProjects, mappedTrainings, response.additionalInfos()
            );

        } catch (Exception e) {
            log.error("기술스택 매핑 실패: {}", e.getMessage(), e);
            return response; // 매핑 실패 시 원본 반환
        }
    }

    private ResumeTechStackSaveRequest mapResumeTechStack(
            ResumeTechStackSaveRequest techStack) {
        
        if (techStack.techStackName() == null || techStack.techStackName().trim().isEmpty()) {
            log.warn("기술스택 이름이 비어있음 - ID: {}", techStack.techStackId());
            return techStack;
        }

        Long realId = techStackMappingService.findTechStackIdByName(techStack.techStackName().trim())
                .orElse(techStack.techStackId());

        if (!realId.equals(techStack.techStackId())) {
            log.info("기술스택 ID 매핑: {} -> {} ({})", techStack.techStackId(), realId, techStack.techStackName());
        }

        return new ResumeTechStackSaveRequest(
                realId, techStack.techStackName(), techStack.proficiencyLevel()
        );
    }

    private CareerTechStackSaveRequest mapCareerTechStack(
            CareerTechStackSaveRequest techStack) {

        if (techStack.techStackName() == null || techStack.techStackName().trim().isEmpty()) {
            return techStack;
        }

        Long realId = techStackMappingService.findTechStackIdByName(techStack.techStackName().trim())
                .orElse(techStack.techStackId());

        return new CareerTechStackSaveRequest(
                realId, techStack.techStackName()
        );
    }

    private ProjectTechStackSaveRequest mapProjectTechStack(
            ProjectTechStackSaveRequest techStack) {

        if (techStack.techStackName() == null || techStack.techStackName().trim().isEmpty()) {
            return techStack;
        }

        Long realId = techStackMappingService.findTechStackIdByName(techStack.techStackName().trim())
                .orElse(techStack.techStackId());

        return new ProjectTechStackSaveRequest(
                realId, techStack.techStackName(), techStack.usageType()
        );
    }

    private TrainingTechStackSaveRequest mapTrainingTechStack(
            TrainingTechStackSaveRequest techStack) {

        if (techStack.techStackName() == null || techStack.techStackName().trim().isEmpty()) {
            return techStack;
        }

        Long realId = techStackMappingService.findTechStackIdByName(techStack.techStackName().trim())
                .orElse(techStack.techStackId());

        return new TrainingTechStackSaveRequest(
                realId, techStack.techStackName()
        );
    }
}