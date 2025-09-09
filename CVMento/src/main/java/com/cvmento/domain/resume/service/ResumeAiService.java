package com.cvmento.domain.resume.service;

import com.cvmento.domain.member.entity.Member;
import com.cvmento.domain.member.repository.MemberRepository;
import com.cvmento.domain.resume.dto.request.UserExperienceRequest;
import com.cvmento.domain.resume.dto.response.ResumeAiSuggestionResponse;
import com.cvmento.global.exception.customException.MemberNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ResumeAiService {

    private final MemberRepository memberRepository;
    private final ResumeAiLlmPromptService promptService;
    private final ResumeAiLlmClientService llmClientService;
    private final ResumeAiResponseParserService responseParserService;

    /**
     * 사용자 경험 기반 이력서 섹션 추가 제안
     */
    public ResumeAiSuggestionResponse generateResumeSuggestions(UserExperienceRequest request, String userEmail) {
        log.info("이력서 섹션 추가 제안 생성 시작. 사용자: {}, 경력구분: {}, 지원분야: {}", 
                userEmail, request.careerType(), request.fieldName());
        
        // 사용자 정보 조회
        Member member = findMemberByEmail(userEmail);
        
        try {
            // 1단계: 프롬프트 생성
            String prompt = promptService.buildFullResumeSuggestionPrompt(request, member);
            log.debug("생성된 프롬프트 길이: {} 문자", prompt.length());
            
            // 2단계: LLM API 호출
            String llmResponse = llmClientService.generateResumeSuggestion(prompt);
            log.info("LLM 응답 수신 완료. 응답 길이: {} 문자", llmResponse.length());
            
            // 3단계: 응답 파싱 및 구조화
            ResumeAiSuggestionResponse response = responseParserService.parseResumeSuggestionResponse(llmResponse, request, member);
            
            log.info("이력서 섹션 추가 제안 생성 완료. 사용자: {}", userEmail);
            return response;
            
        } catch (Exception e) {
            log.error("이력서 섹션 추가 제안 생성 실패. 사용자: {}, 오류: {}", userEmail, e.getMessage(), e);
            throw new RuntimeException("AI 섹션 추가 제안 생성 중 오류가 발생했습니다.", e);
        }
    }

    /**
     * 사용자 정보 조회
     */
    private Member findMemberByEmail(String userEmail) {
        return memberRepository.findByEmail(userEmail)
                .orElseThrow(() -> new MemberNotFoundException("사용자를 찾을 수 없습니다: " + userEmail));
    }

}