package com.cvmento.domain.resume.service;

import com.cvmento.domain.member.entity.Member;
import com.cvmento.domain.member.repository.MemberRepository;
import com.cvmento.domain.resume.dto.request.ResumeCreateRequest;
import com.cvmento.domain.resume.dto.request.ResumeUpdateRequest;
import com.cvmento.domain.resume.dto.response.ResumeResponse;
import com.cvmento.domain.resume.entity.Resume;
import com.cvmento.domain.resume.enums.RecordStatus;
import com.cvmento.domain.resume.repository.ResumeRepository;
import com.cvmento.global.exception.customException.MemberNotFoundException;
import com.cvmento.global.exception.customException.ResumeNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ResumeService {

    private final ResumeRepository resumeRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public ResumeResponse createResume(ResumeCreateRequest request, String userEmail) {
        Member member = findMemberByEmail(userEmail);

        Resume resume = new Resume(
            request.title(),
            member,
            request.intro().selfIntroduction(),
            String.join(",", request.intro().techStack())
        );

        request.sections().forEach(sectionDto -> {
            String combinedContent = sectionDto.items().stream()
                    .map(item -> String.format("Title: %s, SubTitle: %s, Period: %s - %s, Description: %s",
                            item.title(), item.subTitle(), item.startDate(), item.endDate(), item.description()))
                    .reduce((a, b) -> a + "\n---\n" + b)
                    .orElse("");

            resume.addSection(sectionDto.sectionType(), sectionDto.sectionTitle(), combinedContent);
        });

        Resume savedResume = resumeRepository.save(resume);
        return ResumeResponse.from(savedResume);
    }

    @Transactional(readOnly = true)
    public ResumeResponse getResume(Long resumeId, String userEmail) {
        Resume resume = findResumeByIdAndUser(resumeId, userEmail);
        return ResumeResponse.from(resume);
    }

    @Transactional(readOnly = true)
    public List<ResumeResponse> getResumesByMember(String userEmail) {
        Member member = findMemberByEmail(userEmail);
        List<Resume> resumes = resumeRepository.findByMember_MemberId(member.getMemberId());
        return resumes.stream()
                .map(ResumeResponse::from)
                .toList();
    }

    @Transactional
    public ResumeResponse updateResume(Long resumeId, ResumeUpdateRequest request, String userEmail) {
        Resume resume = findResumeByIdAndUser(resumeId, userEmail);

        resume.updateTitle(request.title());
        resume.updateIntro(request.intro().selfIntroduction(), String.join(",", request.intro().techStack()));

        resume.getSections().clear();
        request.sections().forEach(sectionDto -> {
            String combinedContent = sectionDto.items().stream()
                    .map(item -> String.format("Title: %s, SubTitle: %s, Period: %s - %s, Description: %s",
                            item.title(), item.subTitle(), item.startDate(), item.endDate(), item.description()))
                    .reduce((a, b) -> a + "\n---\n" + b)
                    .orElse("");
            resume.addSection(sectionDto.sectionType(), sectionDto.sectionTitle(), combinedContent);
        });

        return ResumeResponse.from(resume);
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
        Member member = findMemberByEmail(userEmail);
        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new ResumeNotFoundException("이력서를 찾을 수 없습니다."));
        
        if (!resume.getMember().getMemberId().equals(member.getMemberId())) {
            throw new ResumeNotFoundException("이력서에 접근할 권한이 없습니다.");
        }
        
        return resume;
    }
}