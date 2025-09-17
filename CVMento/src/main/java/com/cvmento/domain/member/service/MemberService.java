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
import org.slf4j.MDC;
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


    /**
     * 이메일로 회원 조회
     */
    public Member findByEmail(String email) {
        MDC.put("spanId", "member-service");

        MDC.put("spanId", "member-repository");
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new MemberNotFoundException("회원을 찾을 수 없습니다: " + email));

        MDC.put("spanId", "member-service");
        return member;
    }

    /**
     * ID로 회원 조회
     */
    public Member findById(Long memberId) {
        MDC.put("spanId", "member-service");

        MDC.put("spanId", "member-repository");
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberNotFoundException("회원을 찾을 수 없습니다. ID: " + memberId));

        MDC.put("spanId", "member-service");
        return member;
    }

    /**
     * 회원 저장
     */
    @Transactional
    public Member save(Member member) {
        MDC.put("spanId", "member-service");

        MDC.put("spanId", "member-repository");
        Member saved = memberRepository.save(member);

        MDC.put("spanId", "member-service");
        return saved;
    }


    /**
     * 회원 목록 조회 (검색 및 필터링 지원)
     */
    public Page<MemberListResponse> getMemberList(
            Pageable pageable, String email, String name, Role role,
            UserStatus status, String sortBy, String sortDirection) {

        MDC.put("spanId", "member-list-service");

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

        Sort sort = createSort(sortBy, sortDirection);
        Pageable sortedPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);

        MDC.put("spanId", "member-repository");
        Page<Member> memberPage = memberRepository.findAll(spec, sortedPageable);

        MDC.put("spanId", "member-list-service");
        log.info("회원 목록 조회 완료: 총 {}건, 페이지 {}/{}",
                memberPage.getTotalElements(), memberPage.getNumber() + 1, memberPage.getTotalPages());

        return memberPage.map(MemberListResponse::from);
    }

    /**
     * 회원 상세 조회
     */
    public MemberDetailResponse getMemberDetail(Long memberId) {
        MDC.put("spanId", "member-detail-service");

        Member member = findById(memberId);

        MDC.put("spanId", "coverletter-repository");
        int coverLetterCount = coverLetterRepository.findByMember(member).size();

        MDC.put("spanId", "resume-repository");
        int resumeCount = resumeRepository.findByIdAndMember(null, member) != null ?
                member.getResumes().size() : 0;

        MDC.put("spanId", "member-detail-service");
        log.info("회원 상세 조회 완료: memberId={}, email={}, 자소서={}개, 이력서={}개",
                memberId, member.getEmail(), coverLetterCount, resumeCount);

        return MemberDetailResponse.from(member, coverLetterCount, resumeCount);
    }

    /**
     * 회원 상태 변경
     */
    @Transactional
    public void updateMemberStatus(Long memberId, MemberStatusUpdateRequest request, Member admin) {
        MDC.put("spanId", "member-status-update-service");

        Member targetMember = findById(memberId);

        if (targetMember.getMemberId().equals(admin.getMemberId())) {
            throw new IllegalArgumentException("자기 자신의 상태는 변경할 수 없습니다.");
        }

        if (targetMember.getRole() == Role.ROOT && admin.getRole() != Role.ROOT) {
            throw new AccessDeniedException("ROOT 관리자의 상태는 변경할 수 없습니다.");
        }

        UserStatus oldStatus = targetMember.getStatus();

        switch (request.status()) {
            case ACTIVE -> targetMember.activate();
            case INACTIVE -> targetMember.deactivate();
            case SUSPENDED -> targetMember.deactivate(); // SUSPENDED도 비활성화로 처리
        }

        if (request.status() != UserStatus.ACTIVE) {
            forceLogoutMember(targetMember);
        }

        MDC.put("spanId", "member-repository");
        memberRepository.save(targetMember);

        MDC.put("spanId", "member-status-update-service");
        log.info("회원 상태 변경 완료: memberId={}, {}→{}, 관리자={}, 사유={}",
                memberId, oldStatus, request.status(), admin.getEmail(), request.reason());
    }

    /**
     * 회원 역할 변경
     */
    @Transactional
    public void updateMemberRole(Long memberId, MemberRoleUpdateRequest request, Member admin) {
        MDC.put("spanId", "member-role-update-service");

        Member targetMember = findById(memberId);

        if (targetMember.getMemberId().equals(admin.getMemberId())) {
            throw new IllegalArgumentException("자기 자신의 역할은 변경할 수 없습니다.");
        }

        if (request.role() == Role.ROOT && admin.getRole() != Role.ROOT) {
            throw new AccessDeniedException("ROOT 권한 부여는 ROOT 관리자만 가능합니다.");
        }

        if (targetMember.getRole() == Role.ROOT && admin.getRole() != Role.ROOT) {
            throw new AccessDeniedException("ROOT 관리자의 역할은 변경할 수 없습니다.");
        }

        if (targetMember.getRole() == Role.ADMIN && admin.getRole() == Role.ADMIN) {
            throw new AccessDeniedException("동급 관리자의 역할은 변경할 수 없습니다.");
        }

        Role oldRole = targetMember.getRole();
        targetMember.changeRole(request.role());

        if (isRoleDowngraded(oldRole, request.role())) {
            forceLogoutMember(targetMember);
        }

        MDC.put("spanId", "member-repository");
        memberRepository.save(targetMember);

        MDC.put("spanId", "member-role-update-service");
        log.info("회원 역할 변경 완료: memberId={}, {}→{}, 관리자={}, 사유={}",
                memberId, oldRole, request.role(), admin.getEmail(), request.reason());
    }

    /**
     * 회원 통계 조회
     */
    public MemberStatisticsResponse getMemberStatistics() {
        MDC.put("spanId", "member-statistics-service");

        MDC.put("spanId", "member-repository");
        long totalMembers = memberRepository.count();

        long activeMembers = memberRepository.countByStatus(UserStatus.ACTIVE);
        long inactiveMembers = memberRepository.countByStatus(UserStatus.INACTIVE);
        long suspendedMembers = memberRepository.countByStatus(UserStatus.SUSPENDED);

        long userRoleCount = memberRepository.countByRole(Role.USER);
        long adminRoleCount = memberRepository.countByRole(Role.ADMIN);
        long rootRoleCount = memberRepository.countByRole(Role.ROOT);

        LocalDateTime startOfDay = LocalDateTime.now().toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1);
        long todayNewMembers = memberRepository.countByCreatedAtBetween(startOfDay, endOfDay);

        LocalDateTime startOfMonth = LocalDateTime.now().toLocalDate().withDayOfMonth(1).atStartOfDay();
        long monthlyNewMembers = memberRepository.countByCreatedAtAfter(startOfMonth);

        MDC.put("spanId", "member-statistics-service");
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
        MDC.put("spanId", "member-force-logout-service");

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
        MDC.put("spanId", "token-service");
        try {
            tokenService.logout(member.getMemberId().toString(), null, null);
            log.debug("회원 강제 로그아웃 처리 완료: memberId={}", member.getMemberId());
        } catch (Exception e) {
            log.warn("강제 로그아웃 중 오류 발생: memberId={}, error={}", member.getMemberId(), e.getMessage());
        }
        MDC.put("spanId", "member-force-logout-service");
    }
}