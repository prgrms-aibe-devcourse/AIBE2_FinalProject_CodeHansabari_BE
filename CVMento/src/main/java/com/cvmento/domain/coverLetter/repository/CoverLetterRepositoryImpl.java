package com.cvmento.domain.coverLetter.repository;

import com.cvmento.domain.coverLetter.dto.response.CoverLetterStatusListResponse;
import com.cvmento.domain.coverLetter.enums.CoverLetterStatus;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;
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

    /**
     * 관리자 자소서 목록 조회
     */
    @Override
    public Page<CoverLetterStatusListResponse> findCoverLettersWithFilters(
            CoverLetterStatus status,
            String email,
            String title,
            Pageable pageable
    ) {
        // 동적 조건 생성
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(coverLetter.status.eq(status));

        // email 필터 여부 확인 (JOIN 최적화 핵심)
        boolean needsJoin = StringUtils.hasText(email);

        if (needsJoin) {
            builder.and(member.email.containsIgnoreCase(email));
        }
        if (StringUtils.hasText(title)) {
            builder.and(coverLetter.title.containsIgnoreCase(title));
        }

        // 정렬 기준 결정 + 타이브레이커 추가
        OrderSpecifier<?>[] orderBy = (status == CoverLetterStatus.DELETED)
                ? new OrderSpecifier[]{
                coverLetter.updatedAt.asc(),
                coverLetter.coverLetterId.asc()
        }
                : new OrderSpecifier[]{
                coverLetter.updatedAt.desc(),
                coverLetter.coverLetterId.desc()
        };

        // 메인 쿼리 - 조건부 JOIN
        List<CoverLetterStatusListResponse> content;

        if (needsJoin) {
            // email 필터가 있을 때만 JOIN
            content = queryFactory
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
                    .limit(pageable.getPageSize())
                    .fetch();
        } else {
            // email 필터가 없으면 JOIN 없이 조회
            content = queryFactory
                    .select(Projections.constructor(
                            CoverLetterStatusListResponse.class,
                            coverLetter.coverLetterId,
                            Expressions.nullExpression(String.class), // email은 null
                            coverLetter.title,
                            coverLetter.createdAt,
                            coverLetter.updatedAt,
                            coverLetter.status
                    ))
                    .from(coverLetter)
                    .where(builder)
                    .orderBy(orderBy)
                    .offset(pageable.getOffset())
                    .limit(pageable.getPageSize())
                    .fetch();
        }

        // 카운트 쿼리 - 조건부 JOIN
        Long total;
        if (needsJoin) {
            total = queryFactory
                    .select(coverLetter.count())
                    .from(coverLetter)
                    .join(coverLetter.member, member)
                    .where(builder)
                    .fetchOne();
        } else {
            total = queryFactory
                    .select(coverLetter.count())
                    .from(coverLetter)
                    .where(builder)
                    .fetchOne();
        }

        return new PageImpl<>(content, pageable, total != null ? total : 0L);
    }
}