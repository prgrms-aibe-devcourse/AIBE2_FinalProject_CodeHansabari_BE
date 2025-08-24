package com.cvmento.login.service;

import com.cvmento.jwt.TokenBlacklistService;
import com.cvmento.login.dto.SignupRequestDto;
import com.cvmento.member.Member;
import com.cvmento.member.MemberRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final PasswordEncoder passwordEncoder;
    private final MemberRepository memberRepository;
    private final TokenBlacklistService tokenBlacklistService;

    // 일반 가입
    public boolean signup(SignupRequestDto signupRequestDto) {
        // 1. 이메일 중복 확인
        log.info("회원 가입 요청: {}", signupRequestDto);
        if (memberRepository.existsByEmail(signupRequestDto.getEmail())) {
            log.info("이미 존재하는 이메일: {}", signupRequestDto.getEmail());
            return false; // 이메일이 이미 존재하면 false 반환
        }

        // 2. 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(signupRequestDto.getPassword());
        log.info("암호화된 비밀번호 생성 완료 : {}", encodedPassword);

        // 3. 정적 팩토리 메서드로 User 생성
        Member member = Member.create(
                signupRequestDto.getEmail(),
                encodedPassword,
                signupRequestDto.getNickname(),
                signupRequestDto.getPhone(),
                signupRequestDto.getJoinType()
        );

        // 4. User 저장
        memberRepository.save(member);
        log.info("회원 가입 완료: {}", member.getEmail());
        return true; // 회원 가입 성공 시 true 반환
    }

    // 로그아웃 처리 메소드
    public void logout(String email, HttpServletResponse response) throws IOException {

        // 사용자 존재 여부 확인
        validateUserExists(email);
        log.info("로그아웃 요청 - 이메일: {}", email);

        // 활성 통큰 블랙리스트 처리
        tokenBlacklistService.blacklistAllActiveTokens(email);

        // 쿠키 삭제 처리 (Cookie 객체 생성 방식)
        Cookie cookie = new Cookie("token", "");
        cookie.setHttpOnly(true);
        cookie.setSecure(true);            // HTTPS 환경이라면 true, 개발 환경에서는 false로 조절
        cookie.setPath("/");
        cookie.setMaxAge(0);               // 즉시 만료

        response.addCookie(cookie);

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write("{\"message\":\"로그아웃 성공\"}");
    }

    // 사용자 조회 메서드 분리
    private void validateUserExists(String email) {
        memberRepository.findByEmail(email).orElseThrow(() -> {
            log.warn("로그아웃 실패 - 사용자 없음: {}", email);
            return new IllegalArgumentException("사용자가 존재하지 않습니다.");
        });
    }
}
