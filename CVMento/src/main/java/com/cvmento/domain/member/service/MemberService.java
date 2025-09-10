package com.cvmento.domain.member.service;

import com.cvmento.domain.member.dto.request.MemberRoleUpdateRequest;
import com.cvmento.domain.member.dto.request.MemberStatusUpdateRequest;
import com.cvmento.domain.member.dto.response.MemberDetailResponse;
import com.cvmento.domain.member.dto.response.MemberListResponse;
import com.cvmento.domain.member.dto.response.MemberStatisticsResponse;
import com.cvmento.domain.member.entity.Member;
import com.cvmento.domain.member.enums.Role;
import com.cvmento.domain.member.enums.UserStatus;
import com.cvmento.domain.member.repository.MemberRepository;
import com.cvmento.domain.coverLetter.repository.CoverLetterRepository;
import com.cvmento.domain.resume.repository.ResumeRepository;
import com.cvmento.global.exception.customException.MemberNotFoundException;
import com.cvmento.global.security.TokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import jakarta.persistence.criteria.Predicate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;
    private final CoverLetterRepository coverLetterRepository;
    private final ResumeRepository resumeRepository;
    private final TokenService tokenService;

    // ================ 일반 사용자 기능들 ================

    /**
     * 이메일로 회원 조회
     */
    public Member findByEmail(String email) {
        return memberRepository.findByEmail(email)
                .orElseThrow(() -> new MemberNotFoundException("회원을 찾을 수 없습니다: " + email));
    }

    /**
     * ID로 회원 조회
     */
    public Member findById(Long memberId) {
        return memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberNotFoundException("회원을 찾을 수 없습니다. ID: " + memberId));
    }

    /**
     * 회원 저장
     */
    @Transactional
    public Member save(Member member) {
        return memberRepository.save(member);
    }

    // ================ 관리자 기능들 ================

    /**
     * 회원 목록 조회 (검색 및 필터링 지원)
     */
    public Page<MemberListResponse> getMemberList(
            Pageable pageable, String email, String name, Role role,
            UserStatus status, String sortBy, String sortDirection) {

        // 동적 쿼리 생성
        Specification<Member> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (StringUtils.hasText(email)) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("email")),
                        "%" + email.toLowerCase() + "%"));
            }

            if (StringUtils.hasText(name)) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("name")),
                        "%" + name.toLowerCase() + "%"));
            }

            if (role != null) {
                predicates.add(criteriaBuilder.equal(root.get("role"), role));
            }

            if (status != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), status));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        // 정렬 설정
        Sort sort = createSort(sortBy, sortDirection);
        Pageable sortedPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);

        Page<Member> memberPage = memberRepository.findAll(spec, sortedPageable);

        log.info("회원 목록 조회 완료: 총 {}건, 페이지 {}/{}",
                memberPage.getTotalElements(), memberPage.getNumber() + 1, memberPage.getTotalPages());

        return memberPage.map(MemberListResponse::from);
    }

    /**
     * 회원 상세 조회
     */
    public MemberDetailResponse getMemberDetail(Long memberId) {
        Member member = findById(memberId);

        // 자소서 개수 조회
        int coverLetterCount = coverLetterRepository.findByMember(member).size();

        // 이력서 개수 조회
        int resumeCount = resumeRepository.findByMember_MemberId(member.getMemberId()).size();

        log.info("회원 상세 조회 완료: memberId={}, email={}, 자소서={}개, 이력서={}개",
                memberId, member.getEmail(), coverLetterCount, resumeCount);

        return MemberDetailResponse.from(member, coverLetterCount, resumeCount);
    }

    /**
     * 회원 상태 변경
     */
    @Transactional
    public void updateMemberStatus(Long memberId, MemberStatusUpdateRequest request, Member admin) {
        Member targetMember = findById(memberId);

        // 자기 자신의 상태는 변경할 수 없음
        if (targetMember.getMemberId().equals(admin.getMemberId())) {
            throw new IllegalArgumentException("자기 자신의 상태는 변경할 수 없습니다.");
        }

        // ROOT 관리자는 변경할 수 없음 (ROOT가 아닌 경우)
        if (targetMember.getRole() == Role.ROOT && admin.getRole() != Role.ROOT) {
            throw new AccessDeniedException("ROOT 관리자의 상태는 변경할 수 없습니다.");
        }

        UserStatus oldStatus = targetMember.getStatus();

        // 상태 변경
        switch (request.status()) {
            case ACTIVE -> targetMember.activate();
            case INACTIVE -> targetMember.deactivate();
            case SUSPENDED -> targetMember.deactivate(); // SUSPENDED도 비활성화로 처리
        }

        // 상태가 INACTIVE나 SUSPENDED로 변경되면 강제 로그아웃
        if (request.status() != UserStatus.ACTIVE) {
            forceLogoutMember(targetMember);
        }

        memberRepository.save(targetMember);

        log.info("회원 상태 변경 완료: memberId={}, {}→{}, 관리자={}, 사유={}",
                memberId, oldStatus, request.status(), admin.getEmail(), request.reason());
    }

    /**
     * 회원 역할 변경
     */
    @Transactional
    public void updateMemberRole(Long memberId, MemberRoleUpdateRequest request, Member admin) {
        Member targetMember = findById(memberId);

        // 자기 자신의 역할은 변경할 수 없음
        if (targetMember.getMemberId().equals(admin.getMemberId())) {
            throw new IllegalArgumentException("자기 자신의 역할은 변경할 수 없습니다.");
        }

        // ROOT 권한 변경은 ROOT만 가능
        if (request.role() == Role.ROOT && admin.getRole() != Role.ROOT) {
            throw new AccessDeniedException("ROOT 권한 부여는 ROOT 관리자만 가능합니다.");
        }

        // ROOT 관리자의 역할 변경은 ROOT만 가능
        if (targetMember.getRole() == Role.ROOT && admin.getRole() != Role.ROOT) {
            throw new AccessDeniedException("ROOT 관리자의 역할은 변경할 수 없습니다.");
        }

        // ADMIN이 다른 ADMIN의 역할 변경은 불가
        if (targetMember.getRole() == Role.ADMIN && admin.getRole() == Role.ADMIN) {
            throw new AccessDeniedException("동급 관리자의 역할은 변경할 수 없습니다.");
        }

        Role oldRole = targetMember.getRole();
        targetMember.changeRole(request.role());

        // 권한이 축소되면 강제 로그아웃
        if (isRoleDowngraded(oldRole, request.role())) {
            forceLogoutMember(targetMember);
        }

        memberRepository.save(targetMember);

        log.info("회원 역할 변경 완료: memberId={}, {}→{}, 관리자={}, 사유={}",
                memberId, oldRole, request.role(), admin.getEmail(), request.reason());
    }

    /**
     * 회원 통계 조회
     */
    public MemberStatisticsResponse getMemberStatistics() {
        long totalMembers = memberRepository.count();

        long activeMembers = memberRepository.countByStatus(UserStatus.ACTIVE);
        long inactiveMembers = memberRepository.countByStatus(UserStatus.INACTIVE);
        long suspendedMembers = memberRepository.countByStatus(UserStatus.SUSPENDED);

        long userRoleCount = memberRepository.countByRole(Role.USER);
        long adminRoleCount = memberRepository.countByRole(Role.ADMIN);
        long rootRoleCount = memberRepository.countByRole(Role.ROOT);

        // 오늘 가입한 회원 수
        LocalDateTime startOfDay = LocalDateTime.now().toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1);
        long todayNewMembers = memberRepository.countByCreatedAtBetween(startOfDay, endOfDay);

        // 이번 달 가입한 회원 수
        LocalDateTime startOfMonth = LocalDateTime.now().toLocalDate().withDayOfMonth(1).atStartOfDay();
        long monthlyNewMembers = memberRepository.countByCreatedAtAfter(startOfMonth);

        log.info("회원 통계 조회 완료: 전체={}, 활성={}, 오늘가입={}, 이달가입={}",
                totalMembers, activeMembers, todayNewMembers, monthlyNewMembers);

        return MemberStatisticsResponse.builder()
                .totalMembers(totalMembers)
                .activeMembers(activeMembers)
                .inactiveMembers(inactiveMembers)
                .suspendedMembers(suspendedMembers)
                .userRoleCount(userRoleCount)
                .adminRoleCount(adminRoleCount)
                .rootRoleCount(rootRoleCount)
                .todayNewMembers(todayNewMembers)
                .monthlyNewMembers(monthlyNewMembers)
                .build();
    }

    /**
     * 회원 강제 로그아웃
     */
    @Transactional
    public void forceMemberLogout(Long memberId, Member admin) {
        Member targetMember = findById(memberId);

        // 자기 자신은 강제 로그아웃할 수 없음
        if (targetMember.getMemberId().equals(admin.getMemberId())) {
            throw new IllegalArgumentException("자기 자신을 강제 로그아웃할 수 없습니다.");
        }

        // ROOT 관리자는 강제 로그아웃할 수 없음 (ROOT가 아닌 경우)
        if (targetMember.getRole() == Role.ROOT && admin.getRole() != Role.ROOT) {
            throw new AccessDeniedException("ROOT 관리자를 강제 로그아웃할 수 없습니다.");
        }

        forceLogoutMember(targetMember);

        log.info("회원 강제 로그아웃 완료: memberId={}, email={}, 관리자={}",
                memberId, targetMember.getEmail(), admin.getEmail());
    }

    // ================ 내부 헬퍼 메서드들 ================

    /**
     * 정렬 조건 생성
     */
    private Sort createSort(String sortBy, String sortDirection) {
        Sort.Direction direction = "asc".equalsIgnoreCase(sortDirection) ?
                Sort.Direction.ASC : Sort.Direction.DESC;

        return switch (sortBy.toLowerCase()) {
            case "email" -> Sort.by(direction, "email");
            case "name" -> Sort.by(direction, "name");
            case "lastloginat" -> Sort.by(direction, "lastLoginAt");
            case "role" -> Sort.by(direction, "role");
            case "status" -> Sort.by(direction, "status");
            default -> Sort.by(direction, "createdAt");
        };
    }

    /**
     * 역할이 축소되었는지 확인
     */
    private boolean isRoleDowngraded(Role oldRole, Role newRole) {
        int oldLevel = getRoleLevel(oldRole);
        int newLevel = getRoleLevel(newRole);
        return newLevel < oldLevel;
    }

    /**
     * 역할의 권한 레벨 반환
     */
    private int getRoleLevel(Role role) {
        return switch (role) {
            case USER -> 1;
            case ADMIN -> 2;
            case ROOT -> 3;
        };
    }

    /**
     * 회원 강제 로그아웃 처리
     */
    private void forceLogoutMember(Member member) {
        try {
            // 해당 사용자의 모든 토큰 무효화
            tokenService.logout(member.getMemberId().toString(), null, null);
            log.debug("회원 강제 로그아웃 처리 완료: memberId={}", member.getMemberId());
        } catch (Exception e) {
            log.warn("강제 로그아웃 중 오류 발생: memberId={}, error={}", member.getMemberId(), e.getMessage());
            // 로그아웃 실패해도 상태 변경은 진행
        }
    }
}