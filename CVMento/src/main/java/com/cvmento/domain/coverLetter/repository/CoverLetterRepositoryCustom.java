package com.cvmento.domain.coverLetter.repository;

import com.cvmento.domain.coverLetter.dto.response.CoverLetterStatusListResponse;
import com.cvmento.domain.coverLetter.enums.CoverLetterStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * 자소서 Repository 커스텀 인터페이스
 */
public interface CoverLetterRepositoryCustom {

    /**
     * 상태별 자소서 목록을 필터링과 페이징으로 조회
     *
     * @param status 자소서 상태 (ACTIVE, DELETED)
     * @param email 작성자 이메일 (부분 검색, null 가능)
     * @param title 글 제목 (부분 검색, null 가능)
     * @param pageable 페이징 정보
     * @return 자소서 목록 (페이징)
     */
    Page<CoverLetterStatusListResponse> findCoverLettersWithFilters(
            CoverLetterStatus status,
            String email,
            String title,
            Pageable pageable
    );
}