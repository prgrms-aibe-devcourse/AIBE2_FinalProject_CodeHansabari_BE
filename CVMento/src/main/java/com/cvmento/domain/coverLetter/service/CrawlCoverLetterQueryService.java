package com.cvmento.domain.coverLetter.service;

import com.cvmento.domain.coverLetter.dto.response.CrawlCoverLetterData;
import com.cvmento.domain.coverLetter.entity.CrawlCoverLetter;
import com.cvmento.domain.coverLetter.repository.CrawlCoverLetterRepository;
import com.cvmento.global.exception.customException.CrawlCoverLetterException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class CrawlCoverLetterQueryService {

    private final CrawlCoverLetterRepository crawlCoverLetterRepository;

    /**
     * 크롤링 데이터 페이징 조회
     */
    public Page<CrawlCoverLetterData> getCrawlCoverLettersWithPagination(Pageable pageable) {
        MDC.put("spanId", "crawl-pagination-service");
        Page<CrawlCoverLetter> coverLetterPage = crawlCoverLetterRepository.findAll(pageable);
        log.info("크롤링 데이터 페이징 조회 완료 - 페이지: {}, 크기: {}, 총 개수: {}",
                pageable.getPageNumber(), pageable.getPageSize(), coverLetterPage.getTotalElements());
        return coverLetterPage.map(CrawlCoverLetterData::from);
    }

    /**
     * 크롤링 데이터 단건 조회
     */
    public CrawlCoverLetterData getCrawlCoverLetterById(Long id) {
        MDC.put("spanId", "crawl-detail-service");
        CrawlCoverLetter coverLetter = crawlCoverLetterRepository.findById(id)
                .orElseThrow(() -> new CrawlCoverLetterException(
                    "크롤링 데이터를 찾을 수 없습니다. ID: " + id
                ));
        log.info("크롤링 데이터 개별 조회 완료 - ID: {}, 텍스트길이: {}",
                id, coverLetter.getText() != null ? coverLetter.getText().length() : 0);
        return CrawlCoverLetterData.from(coverLetter);
    }
}