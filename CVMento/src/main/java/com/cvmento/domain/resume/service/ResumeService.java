package com.cvmento.domain.resume.service;

import com.cvmento.domain.member.entity.Member;
import com.cvmento.domain.member.repository.MemberRepository;
import com.cvmento.domain.resume.dto.request.ResumeCreateRequest;
import com.cvmento.domain.resume.dto.request.ResumeUpdateRequest;
import com.cvmento.domain.resume.dto.response.ResumeResponse;
import com.cvmento.domain.resume.entity.Resume;
import com.cvmento.domain.resume.repository.ResumeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ResumeService {

    private final ResumeRepository resumeRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public ResumeResponse createResume(ResumeCreateRequest request) {
        // TODO: 현재 로그인한 사용자 정보를 가져오도록 수정해야 함
        Member member = memberRepository.findById(1L)
                .orElseThrow(() -> new IllegalArgumentException("Member not found"));

        Resume resume = new Resume(request.title(), member);

        request.sections().forEach(sectionDto -> {
            String combinedContent = sectionDto.items().stream()
                    .map(item -> String.format("Title: %s, SubTitle: %s, Period: %s - %s, Description: %s",
                            item.title(), item.subTitle(), item.startDate(), item.endDate(), item.description()))
                    .collect(Collectors.joining("\n---\n"));

            resume.addSection(sectionDto.sectionType(), sectionDto.sectionTitle(), combinedContent);
        });

        Resume savedResume = resumeRepository.save(resume);
        return ResumeResponse.from(savedResume);
    }

    @Transactional(readOnly = true)
    public ResumeResponse getResume(Long resumeId) {
        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new IllegalArgumentException("Resume not found"));
        return ResumeResponse.from(resume);
    }

    @Transactional(readOnly = true)
    public List<ResumeResponse> getResumesByMember(Long memberId) {
        List<Resume> resumes = resumeRepository.findByMember_MemberId(memberId);
        return resumes.stream()
                .map(ResumeResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public ResumeResponse updateResume(Long resumeId, ResumeUpdateRequest request) {
        Resume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new IllegalArgumentException("Resume not found"));

        // TODO: Check if the resume belongs to the current user

        resume.updateTitle(request.title());

        resume.getSections().clear();
        request.sections().forEach(sectionDto -> {
            String combinedContent = sectionDto.items().stream()
                    .map(item -> String.format("Title: %s, SubTitle: %s, Period: %s - %s, Description: %s",
                            item.title(), item.subTitle(), item.startDate(), item.endDate(), item.description()))
                    .collect(Collectors.joining("\n---\n"));
            resume.addSection(sectionDto.sectionType(), sectionDto.sectionTitle(), combinedContent);
        });

        return ResumeResponse.from(resume);
    }

    @Transactional
    public void deleteResume(Long resumeId) {
        // TODO: Check if the resume belongs to the current user
        if (!resumeRepository.existsById(resumeId)) {
            throw new IllegalArgumentException("Resume not found");
        }
        resumeRepository.deleteById(resumeId);
    }
}