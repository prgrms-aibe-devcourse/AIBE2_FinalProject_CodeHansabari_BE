package com.cvmento.domain.coverLetter.repository;

import com.cvmento.domain.coverLetter.dto.response.CoverLetterStatusListResponse;
import com.cvmento.domain.coverLetter.enums.CoverLetterStatus;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.List;

import static com.cvmento.domain.coverLetter.entity.QCoverLetter.coverLetter;
import static com.cvmento.domain.member.entity.QMember.member;

/**
 * 자소서 Repository 커스텀 구현체
 */
@Repository
@RequiredArgsConstructor
public class CoverLetterRepositoryImpl implements CoverLetterRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<CoverLetterStatusListResponse> findCoverLettersWithFilters(
            CoverLetterStatus status,
            String email,
            String title,
            Pageable pageable
    ) {
        // 동적 조건 생성
        BooleanBuilder builder = new BooleanBuilder();

        // 기본 조건: 상태
        builder.and(coverLetter.status.eq(status));

        // 이메일 필터링 (부분 검색)
        if (StringUtils.hasText(email)) {
            builder.and(member.email.containsIgnoreCase(email));
        }

        // 제목 필터링 (부분 검색)
        if (StringUtils.hasText(title)) {
            builder.and(coverLetter.title.containsIgnoreCase(title));
        }

        // 정렬 기준 결정 (삭제된 상태일 때만 삭제 예정일 임박순)
        var orderBy = status == CoverLetterStatus.DELETED
                ? coverLetter.updatedAt.asc()  // 삭제 예정일 임박순
                : coverLetter.updatedAt.desc(); // 최신 수정일순

        // 메인 쿼리 - 데이터 조회
        JPAQuery<CoverLetterStatusListResponse> query = queryFactory
                .select(Projections.constructor(
                        CoverLetterStatusListResponse.class,
                        coverLetter.coverLetterId,
                        member.email,
                        coverLetter.title,
                        coverLetter.createdAt,
                        coverLetter.updatedAt,
                        coverLetter.status
                ))
                .from(coverLetter)
                .join(coverLetter.member, member)
                .where(builder)
                .orderBy(orderBy)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize());

        List<CoverLetterStatusListResponse> content = query.fetch();

        // 카운트 쿼리 - 전체 개수 조회
        Long total = queryFactory
                .select(coverLetter.count())
                .from(coverLetter)
                .join(coverLetter.member, member)
                .where(builder)
                .fetchOne();

        return new PageImpl<>(content, pageable, total != null ? total : 0L);
    }
}