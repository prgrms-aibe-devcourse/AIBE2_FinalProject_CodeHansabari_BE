package com.cvmento.domain.resume.service;

import com.cvmento.domain.member.entity.Member;
import com.cvmento.domain.member.repository.MemberRepository;
import com.cvmento.domain.resume.dto.request.ResumeCreateRequest;
import com.cvmento.domain.resume.dto.request.ResumeRequest;
import com.cvmento.domain.resume.dto.request.ResumeUpdateRequest;
import com.cvmento.domain.resume.dto.response.ResumeResponse;
import com.cvmento.domain.resume.entity.Resume;
import com.cvmento.domain.resume.entity.ResumeSection;
import com.cvmento.domain.resume.enums.RecordStatus;
import com.cvmento.domain.resume.repository.ResumeRepository;
import com.cvmento.domain.resume.util.TechStackConverter;
import com.cvmento.global.exception.customException.MemberNotFoundException;
import com.cvmento.global.exception.customException.ResumeNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ResumeService {

    private final ResumeRepository resumeRepository;
    private final MemberRepository memberRepository;
    private final TechStackConverter techStackConverter;

    @Transactional
    public ResumeResponse createResume(ResumeCreateRequest request, String userEmail) {
        Member member = findMemberByEmail(userEmail);
        Resume resume = createResumeEntity(request, member);
        addSectionsToResume(resume, request.sections());
        
        Resume savedResume = resumeRepository.save(resume);
        List<String> convertedTechStack = techStackConverter.fromJson(savedResume.getTechStack());
        return ResumeResponse.from(savedResume, convertedTechStack);
    }
    
    private Resume createResumeEntity(ResumeCreateRequest request, Member member) {
        String selfIntroduction = request.memberInfo().introduction();
        List<String> techStack = request.memberInfo().techStack() != null 
            ? request.memberInfo().techStack() 
            : Collections.emptyList();
        
        String techStackJson = techStackConverter.toJson(techStack);
        
        return new Resume(
            request.title(),
            member,
            selfIntroduction,
            techStackJson
        );
    }
    
    private void addSectionsToResume(Resume resume, List<ResumeRequest.ResumeSectionRequest> sectionRequests) {
        sectionRequests.forEach(sectionDto -> {
            String combinedContent = createCombinedContent(sectionDto);
            ResumeSection section = new ResumeSection(sectionDto.sectionType(), sectionDto.sectionTitle(), combinedContent, resume);
            addItemsToSection(section, sectionDto.items());
            resume.getSections().add(section);
        });
    }
    
    private String createCombinedContent(ResumeRequest.ResumeSectionRequest sectionDto) {
        return sectionDto.items().stream()
                .map(item -> String.format("Title: %s, SubTitle: %s, Period: %s - %s, Description: %s",
                        item.title(), item.subTitle(), item.startDate(), item.endDate(), item.description()))
                .reduce((a, b) -> a + "\n---\n" + b)
                .orElse("");
    }
    
    private void addItemsToSection(ResumeSection section, List<ResumeRequest.SectionItemRequest> items) {
        items.forEach(itemDto -> {
            LocalDate startDate = parseDate(itemDto.startDate());
            LocalDate endDate = parseDate(itemDto.endDate());
            section.addItem(itemDto.title(), itemDto.subTitle(), startDate, endDate, itemDto.description());
        });
    }

    @Transactional(readOnly = true)
    public ResumeResponse getResume(Long resumeId, String userEmail) {
        Resume resume = findResumeByIdAndUser(resumeId, userEmail);
        List<String> convertedTechStack = techStackConverter.fromJson(resume.getTechStack());
        return ResumeResponse.from(resume, convertedTechStack);
    }

    @Transactional(readOnly = true)
    public List<ResumeResponse> getResumesByMember(String userEmail) {
        Member member = findMemberByEmail(userEmail);
        List<Resume> resumes = resumeRepository.findByMember_MemberId(member.getMemberId());
        return resumes.stream()
                .map(resume -> {
                    List<String> convertedTechStack = techStackConverter.fromJson(resume.getTechStack());
                    return ResumeResponse.from(resume, convertedTechStack);
                })
                .toList();
    }

    @Transactional
    public ResumeResponse updateResume(Long resumeId, ResumeUpdateRequest request, String userEmail) {
        Resume resume = findResumeByIdAndUser(resumeId, userEmail);

        resume.updateTitle(request.title());
        
        // 기술스택을 JSON으로 변환
        String techStackJson = techStackConverter.toJson(request.memberInfo().techStack());
        
        resume.updateIntro(
            request.memberInfo().introduction(), 
            techStackJson != null ? techStackJson : ""
        );

        resume.getSections().clear();
        request.sections().forEach(sectionDto -> {
            String combinedContent = sectionDto.items().stream()
                    .map(item -> String.format("Title: %s, SubTitle: %s, Period: %s - %s, Description: %s",
                            item.title(), item.subTitle(), item.startDate(), item.endDate(), item.description()))
                    .reduce((a, b) -> a + "\n---\n" + b)
                    .orElse("");
            resume.addSection(sectionDto.sectionType(), sectionDto.sectionTitle(), combinedContent);
        });

        List<String> convertedTechStack = techStackConverter.fromJson(resume.getTechStack());
        return ResumeResponse.from(resume, convertedTechStack);
    }

    @Transactional
    public void deleteResume(Long resumeId, String userEmail) {
        Resume resume = findResumeByIdAndUser(resumeId, userEmail);
        resume.setStatus(RecordStatus.DELETED);
    }

    private Member findMemberByEmail(String userEmail) {
        return memberRepository.findByEmail(userEmail)
                .orElseThrow(() -> new MemberNotFoundException("사용자를 찾을 수 없습니다."));
    }

    private Resume findResumeByIdAndUser(Long resumeId, String userEmail) {
        // EntityGraph를 사용하여 연관 엔티티를 한 번에 로드
        Resume resume = resumeRepository.findByResumeIdAndMember_Email(resumeId, userEmail)
                .orElseThrow(() -> new ResumeNotFoundException("이력서를 찾을 수 없습니다."));
        
        return resume;
    }

    // 날짜 문자열을 LocalDate로 변환하는 헬퍼 메서드
    private LocalDate parseDate(String dateString) {
        if (dateString == null || dateString.trim().isEmpty() || "null".equals(dateString)) {
            return null;
        }
        try {
            return LocalDate.parse(dateString, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        } catch (Exception e) {
            return null; // 파싱 실패 시 null 반환
        }
    }
}